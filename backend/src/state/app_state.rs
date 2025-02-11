use std::{collections::HashMap, sync::Mutex};

use tauri::Emitter;

use crate::trees::{DebugTree, DebugNode};

use super::StateManager;


pub struct AppState(Mutex<AsyncAppState>);

impl AppState {
    pub fn new(app_handle: tauri::AppHandle) -> AppState {
        AppState(Mutex::new(AsyncAppState::new(app_handle)))
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
    fn set_tree(&self, tree: DebugTree) {
        self.0.lock()
            .expect("Failed to acquire lock")
            .set_tree(tree);
    }
    
    fn get_tree(&self) -> DebugTree {
        self.0.lock()
            .expect("Failed to acquire lock")
            .tree
            .as_ref()
            .expect("Tree has not been loaded")
            .clone()
    }
    
    fn get_debug_node(&self, node_id: u32) -> DebugNode {
        self.0.lock()
            .expect("Failed to acquire lock")
            .map.get(&node_id)
            .expect("Tree has not been loaded")
            .clone()
    }
}
