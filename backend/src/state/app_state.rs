use std::collections::HashMap;
use std:: sync::{Mutex, MutexGuard};

use tauri::Emitter;

use super::{StateError, StateManager};
use crate::trees::{DebugTree, DebugNode};



pub struct AppState(Mutex<AsyncAppState>);

impl AppState {
    pub fn new(app_handle: tauri::AppHandle) -> AppState {
        let async_app_state: AsyncAppState = AsyncAppState::new(app_handle);
        let sync_app_state: Mutex<AsyncAppState> = Mutex::new(async_app_state);

        AppState(sync_app_state)
    }

    fn acquire(&self) -> Result<MutexGuard<AsyncAppState>, StateError> {
        self.0.lock()
            .map_err(|_| StateError::LockFailed)
    }

}


/* Global app state managed by Tauri */
struct AsyncAppState {
    app_handle: tauri::AppHandle,
    tree: Option<DebugTree>, /* Parser tree that we may have */
    map: HashMap<u32, DebugNode>, /* Map from node_id to the respective node */
}

impl AsyncAppState {
    /* Create new AppState with no parser */
    fn new(app_handle: tauri::AppHandle) -> Self {
        AsyncAppState {
            app_handle,
            tree: None,
            map: HashMap::new(),
        }
    }

    fn setup_map(&mut self, node: &DebugNode) {
        self.map.insert(node.node_id, node.clone());
        node.children.iter().for_each(|child| self.setup_map(child));
    }

    fn set_tree(&mut self, tree: DebugTree) {
        self.map.clear();
        self.setup_map(tree.get_root());
        self.tree = Some(tree);        
        
        /* Notify frontend listener */
        self.app_handle.emit("tree-ready", ()).expect("Could not find ready tree");

    }

}


impl StateManager for AppState {
    fn set_tree(&self, tree: DebugTree) -> Result<(), StateError> {
        self.acquire()?.set_tree(tree);
        Ok(())
    }
    
    fn get_tree(&self) -> Result<DebugTree, StateError> {
        self.acquire()?
            .tree
            .as_ref()
            .ok_or(StateError::TreeNotFound)
            .cloned()
    }
    
    fn get_node(&self, id: u32) -> Result<DebugNode, StateError> {
        self.acquire()?
            .map
            .get(&id)
            .ok_or(StateError::NodeNotFound(id))
            .cloned()
    }
}
