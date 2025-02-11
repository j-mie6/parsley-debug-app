use std::sync::Mutex;

use tauri::{Emitter, Manager};
#[cfg(test)] use mockall::automock;

use super::AppState;
use crate::trees::{DebugTree, DebugNode};


#[cfg_attr(test, automock)]
pub trait StateManager: Send + Sync + 'static {
    fn set_tree(&self, tree: DebugTree);

    fn get_tree(&self) -> DebugTree;

    fn get_debug_node(&self, node_id: u32) -> DebugNode;
}


impl StateManager for tauri::AppHandle {
    fn set_tree(&self, tree: DebugTree) {
        self.state::<Mutex<AppState>>()
            .lock()
            .expect("Failed to acquire lock")
            .set_tree(tree);

        /* EMIT TO FRONTEND FROM HERE */
        self.emit("tree-ready", ())
            .expect("Could not find ready tree");
    }

    fn get_tree(&self) -> DebugTree {
        self.state::<Mutex<AppState>>()
            .lock()
            .expect("Failed to acquire lock")
            .get_tree()
            .expect("Tree has not been loaded")
            .clone()
    }

    fn get_debug_node(&self, node_id: u32) -> DebugNode {
        self.state::<Mutex<AppState>>()
            .lock()
            .expect("Failed to acquire lock")
            .get_debug_node(node_id)
            .expect(format!("Expected a debug node for {}", node_id).as_str())
            .clone()
    }
}
