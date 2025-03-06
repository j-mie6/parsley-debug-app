use std::collections::HashMap;
use std::sync::{Mutex, MutexGuard};

use crate::events::Event;
use crate::server::TokioMutex;
use crate::trees::{DebugTree, DebugNode};

pub type SkipsSender = rocket::tokio::sync::mpsc::Sender<i32>;

use super::{StateError, StateManager, AppHandle};

/* Unsynchronised AppState */
struct AppStateInternal {
    app: AppHandle,                 /* Handle to instance of Tauri app, used for events */
    tree: Option<DebugTree>,        /* Parser tree that is posted to Server */
    map: HashMap<u32, DebugNode>,   /* Map from node_id to the respective node */
    skips_tx: TokioMutex<SkipsSender>, /* Transmitter how many breakpoints to skip, sent to parsley */
    tab_names: Vec<String>,         /* List of saved tree names */
}


/* Synchronised global app state wrapping internal AppState */
pub struct AppState(Mutex<AppStateInternal>);

impl AppState {
    /* Create a new app state with the app_handle */
    pub fn new(app_handle: tauri::AppHandle, skips_tx: TokioMutex<SkipsSender>) -> AppState {
        AppState(
            Mutex::new(
                AppStateInternal {
                    app: AppHandle::new(app_handle),
                    tree: None,
                    map: HashMap::new(),
                    skips_tx,
                    tab_names: Vec::new(),
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
        let event: Event = Event::TreeReady(&state.tree.as_ref().unwrap());
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

    fn transmit_breakpoint_skips(&self, skips: i32) -> Result<(), StateError> {
        self.inner()?
            .skips_tx
            .blocking_lock()
            .try_send(skips)
            .map_err(|_| StateError::ChannelError)
    }
}
