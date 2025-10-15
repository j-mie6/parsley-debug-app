use std::collections::HashMap;
use std::fs::File;
use std::io::Write;
use std::sync::{Mutex, MutexGuard};
use std::path::PathBuf;

use crate::events::Event;
use crate::files::SAVED_TREE_DIR;
use crate::state::state_manager::BreakpointCode;
use crate::trees::{DebugNode, DebugTree, SavedTree};

pub type SkipsSender = rocket::tokio::sync::oneshot::Sender<i32>;

use super::session_counter::SessionCounter;
use super::state_manager::{DirectoryKind, UpdateTreeError};
use super::{StateError, StateManager, AppHandle};

/* Unsynchronised AppState */
struct AppStateInternal {
    app: AppHandle,                                 /* Handle to instance of Tauri app, used for events */
    tree: Option<DebugTree>,                        /* Parser tree that is posted to Server */
    map: HashMap<u32, DebugNode>,                   /* Map from node_id to the respective node */
    skips_tx: HashMap<i32, SkipsSender>,            /* Transmitter how many breakpoints to skip, sent to parsley */
    tab_names: Vec<String>,                         /* List of saved tree names */
    debug_sessions: HashMap<String, i32>,           /* Map of tree name to sessionId */
    saved_refs: HashMap<i32, Vec<(i32, String)>>,   /* Map of sessionId to saved refs for a tab */
    counter: SessionCounter                         /* Counter to hold next sessionId */
}


/* Synchronised global app state wrapping internal AppState */
pub struct AppState(Mutex<AppStateInternal>);

impl AppState {
    /* Create a new app state with the app_handle */
    pub fn new(app_handle: tauri::AppHandle) -> AppState {
        AppState(
            Mutex::new(
                AppStateInternal {
                    app: AppHandle::new(app_handle),
                    tree: None,
                    map: HashMap::new(),
                    skips_tx: HashMap::new(),
                    tab_names: Vec::new(),
                    debug_sessions: HashMap::new(),
                    saved_refs: HashMap::new(),
                    counter: SessionCounter::new(),
                }
            )
        )
    }

    /* Access wrapped inner AppStateInternal struct */
    fn inner(&self) -> Result<MutexGuard<AppStateInternal>, StateError> {
        self.0.lock()
            .map_err(|_| StateError::LockFailed)
    }

    /* Add tree name to tab_names */
    pub fn add_tree(&self, new_tab: String) -> Result<Vec<String>, StateError> {
        let mut state: MutexGuard<AppStateInternal> = self.inner()?;

        /* Add new tree name and return all saved tree names */
        state.tab_names.push(new_tab);

        Ok(state.tab_names.clone())
    }

    /* Remove tree name from tab_names */
    pub fn rmv_tree(&self, index: usize) -> Result<Vec<String>, StateError> {
        let mut state: MutexGuard<AppStateInternal> = self.inner()?;

        /* Remove tree name at index */
        state.tab_names.remove(index);

        /* If this was the final tree then the loaded tree needs to be removed */
        if state.tab_names.is_empty() {
            state.tree = None
        }

        Ok(state.tab_names.clone())
    }

    /* Given an index returns the tree name */
    pub fn get_tree_name(&self, index: usize) -> Result<String, StateError> {
        let state: MutexGuard<AppStateInternal> = self.inner()?;
        
        /* Index will never be out of range as the frontend representation and the backend will be in sync */
        Ok(state.tab_names[index].clone()) 
    }

    pub fn update_refs(&self, session_id: i32, new_refs: Vec<(i32, String)>) -> Result<(), StateError> {
        let mut state: MutexGuard<AppStateInternal> = self.inner()?;

        state.saved_refs.insert(session_id, new_refs);

        Ok(())
    }
}


impl StateManager for AppState {
    /* Update StateManager's tree */
    fn set_tree(&self, tree: DebugTree) -> Result<(), StateError> {
        let mut state: MutexGuard<AppStateInternal> = self.inner()?;
        
        /* Reset map */
        state.map.clear();
        
        /* Recursively insert nodes into map of node ids to nodes */
        fn insert_node(state: &mut MutexGuard<AppStateInternal>, node: &DebugNode) -> Result<(), StateError> {
            state.map.insert(node.node_id, node.clone());

            for child in node.children.iter() {
                insert_node(state, child)?;
            }
            
            Ok(())
        }

        insert_node(&mut state, tree.get_root())?;

        
        /* Update tree */
        state.tree = Some(tree);
    
        /* Unwrap cannot fail as tree has just been set */
        let event: Event = Event::TreeReady(state.tree.as_ref().unwrap());
        state.app.emit(event) /* Notify frontend listener - call inline to avoid deadlock */ 
            .map_err(|_| StateError::EventEmitFailed)
    }
    
    /* Get StateManager's tree */
    fn get_tree(&self) -> Result<DebugTree, StateError> {
        self.inner()?
            .tree
            .as_ref()
            .ok_or(StateError::TreeNotFound)
            .cloned()
    }
    
    /* Get node associated with node ID */
    fn get_node(&self, id: u32) -> Result<DebugNode, StateError> {
        self.inner()?
            .map
            .get(&id)
            .ok_or(StateError::NodeNotFound(id))
            .cloned()
    }

    /* Notify frontend listeners of an event */        
    fn emit<'a>(&self, event: Event<'a>) -> Result<(), StateError> {
        self.inner()?.app.emit(event)
    }

    fn transmit_breakpoint_skips(&self, session_id: i32, code: BreakpointCode) -> Result<(), StateError> {
        let val = match code {
            BreakpointCode::Skip(skips) => skips,
            BreakpointCode::SkipAll => -1,
            BreakpointCode::Terminate => -2,
        };
        self.inner()?
            .skips_tx
            .remove(&session_id)
            .ok_or(StateError::ChannelError)?
            .send(val)
            .map_err(|_| StateError::ChannelError)
    }

    fn system_path(&self, dir: super::state_manager::DirectoryKind) -> Result<PathBuf, StateError> {
        let handle: &AppHandle = &self.inner()?.app;

        match dir {
            DirectoryKind::SavedTrees => handle.tauri_temp_dir()
                                               .map(|path| path.join(SAVED_TREE_DIR)),
            DirectoryKind::Downloads => handle.tauri_downloads_dir(),
        }
    }
    
    fn add_session_id(&self, tree_name: String, session_id:i32) -> Result<(), StateError> {
        self.inner()?
            .debug_sessions
            .insert(tree_name, session_id);

        Ok(())
    }
    
    fn rmv_session_id(&self, tree_name: String) -> Result<(), StateError> {
        let mut state: MutexGuard<'_, AppStateInternal> = self.inner()?;

        state
            .debug_sessions
            .remove(&tree_name);

        if let Some(session_id) = state.debug_sessions.get(&tree_name).cloned() {
            state.saved_refs.remove(&session_id);
        }

        Ok(())
    }
    
    fn session_id_exists(&self, session_id:i32) -> Result<bool, StateError> {
        let state: MutexGuard<'_, AppStateInternal> = self.inner()?;

        Ok(state.debug_sessions.values().any(|&id| id == session_id))
    }

    fn get_session_ids(&self) -> Result<HashMap<String, i32>, StateError> {
        let state: MutexGuard<AppStateInternal> = self.inner()?;
        
        Ok(state.debug_sessions.clone())
    }

    fn next_session_id(&self) -> Result<i32, StateError> {
        let mut state: MutexGuard<'_, AppStateInternal> = self.inner()?;
        
        Ok(state.counter.get_and_increment())
    }

    fn new_transmitter(&self, session_id: i32, tx: SkipsSender) -> Result<(), StateError> {
        match self.inner()?
            .skips_tx
            .insert(session_id, tx) {
                Some(_) => Err(StateError::ChannelError),
                None => Ok(())
            }
    }

    fn get_refs(&self, session_id: i32) -> Result<Vec<(i32, String)>, StateError> {
        let state: MutexGuard<AppStateInternal> = self.inner()?;

        let refs_opt: Option<&Vec<(i32, String)>> = state.saved_refs.get(&session_id);

        match refs_opt {
            Some(refs) => Ok(refs.clone()),
            None => Ok(Vec::new()),
        }
    }

    fn reset_refs(&self, session_id: i32, default_refs: Vec<(i32, String)>) -> Result<(), StateError> {
        let mut state: MutexGuard<AppStateInternal> = self.inner()?;

        state.saved_refs.insert(session_id, default_refs);

        Ok(())
    }

    fn reset_trees(&self) -> Result<(), StateError> {
        let mut state: MutexGuard<AppStateInternal> = self.inner()?;

        state.tab_names = Vec::new();
        state.debug_sessions = HashMap::new();
        state.saved_refs = HashMap::new();

        Ok(())
    }

    fn update_tree(&self, tree: &DebugTree, tree_name: String) -> Result<(), UpdateTreeError> {
        let new_tree: SavedTree = SavedTree::from(tree.clone());
        /* Get the serialised JSON */
        let tree_json: String = serde_json::to_string_pretty(&new_tree)
            .map_err(|_| UpdateTreeError::SerialiseFailed)?;

        /* Open the json file to update the tree */
        /* TODO: look into only updating the extra bits rather than replacing the tree */
        let file_path = PathBuf::from(format!("{}.json", tree_name));
        let full_path: PathBuf = self.system_path_to(DirectoryKind::SavedTrees, file_path).map_err(|_| UpdateTreeError::OpenFileFailed)?;

        let mut data_file: File = File::create(full_path).map_err(|_| UpdateTreeError::OpenFileFailed)?;

        /* Write tree json to the json file */
        data_file.write(tree_json.as_bytes()).map_err(|_| UpdateTreeError::WriteTreeFailed)?;

        Ok(())
    }
}
