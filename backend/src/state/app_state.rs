use std::collections::HashMap;
use std:: sync::{Mutex, MutexGuard};

use tauri::Emitter;

use super::{StateError, StateManager};
use crate::trees::{DebugTree, DebugNode};

/* Synchronised global app state wrapping AppStateInternal */
pub struct AppState(Mutex<AppStateInternal>);

impl AppState {
    /* Create a new app state with the app_handle */
    pub fn new(app_handle: tauri::AppHandle) -> AppState {
        AppState(
            Mutex::new(
                AppStateInternal::new(app_handle)
            )
        )
    }

    /* Acquire the lock on the AppStateInternal */
    fn acquire(&self) -> Result<MutexGuard<AppStateInternal>, StateError> {
        self.0.lock()
            .map_err(|_| StateError::LockFailed)
    }

}


/* Unsynchronised global AppState */
struct AppStateInternal {
    app: tauri::AppHandle,          /* Handle to instance of Tauri app, used for events */
    tree: Option<DebugTree>,        /* Parser tree that is posted to Server */
    map: HashMap<u32, DebugNode>,   /* Map from node_id to the respective node */
}

impl AppStateInternal {
    
    /* Create new AppStateInternal with no parser */
    fn new(app_handle: tauri::AppHandle) -> Self {
        AppStateInternal {
            app: app_handle,
            tree: None,
            map: HashMap::new(),
        }
    }

    /* Recursively insert nodes into map of node ids to nodes */
    fn insert_node(&mut self, node: &DebugNode) {
        self.map.insert(node.node_id, node.clone());
        node.children.iter().for_each(|child| self.insert_node(child));
    }
    
}

impl StateManager for AppState {

    /* Update StateManager's tree */
    fn set_tree(&self, tree: DebugTree) -> Result<(), StateError> {
        let mut state: MutexGuard<AppStateInternal> = self.acquire()?;
        
        /* Reset map */
        state.map.clear();
        state.insert_node(tree.get_root());

        /* Update tree */
        state.tree = Some(tree);
        
        /* Notify frontend listener */
        state.app.emit("tree-ready", ()).map_err(|_| StateError::EventEmitFailed)
    }
    
    /* Get StateManager's tree */
    fn get_tree(&self) -> Result<DebugTree, StateError> {
        self.acquire()?
            .tree
            .as_ref()
            .ok_or(StateError::TreeNotFound)
            .cloned()
    }
    
    /* Get node associated with node ID */
    fn get_node(&self, id: u32) -> Result<DebugNode, StateError> {
        self.acquire()?
            .map
            .get(&id)
            .ok_or(StateError::NodeNotFound(id))
            .cloned()
    }
    
}
