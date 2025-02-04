#[cfg(test)] use mockall::automock;
use tauri::Manager;
use std::sync::Mutex;

use crate::{AppState, DebugTree, DebugNode};

pub struct StateHandle(Box<dyn StateManager>);

#[cfg_attr(test, automock)]
pub trait StateManager : Send + Sync + 'static {
    fn set_tree(&self, tree: DebugTree);

    fn get_tree(&self) -> DebugTree;

    fn get_debug_node(&self, node_id: u32) -> DebugNode;
}

impl StateHandle {
    pub fn new<S : StateManager>(state: S) -> Self {
        StateHandle(Box::new(state))
    }
}

impl StateManager for StateHandle {
    fn set_tree(&self, tree: DebugTree) {
        self.0.as_ref().set_tree(tree);
    }

    fn get_tree(&self) -> DebugTree {
        self.0.as_ref().get_tree()
    }

    fn get_debug_node(&self, node_id: u32) -> DebugNode {
        self.0.as_ref().get_debug_node(node_id)
    }
}

impl StateManager for tauri::AppHandle {
    fn set_tree(&self, tree: DebugTree) {
        self.state::<Mutex<AppState>>()
            .lock()
            .expect("Failed to acquire lock")
            .set_tree(tree);
    }
    
    fn get_tree(&self) -> DebugTree {
        self.state::<Mutex<AppState>>()
            .lock()
            .expect("Failed to acquire lock")
            .tree
            .as_ref()
            .expect("Tree has not been loaded")
            .clone()
    }

    fn get_debug_node(&self, node_id: u32) -> DebugNode {
        self.state::<Mutex<AppState>>()
            .lock()
            .expect("Failed to acquire lock")
            .get_debug_node(node_id)
            .unwrap_or_else(|| panic!("Expected a debug node for {}", node_id))
            .clone()
    }
}
