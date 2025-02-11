use tauri::Manager;
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
        self.state::<AppState>().set_tree(tree);
    }

    fn get_tree(&self) -> DebugTree {
        self.state::<AppState>().get_tree()
    }

    fn get_debug_node(&self, node_id: u32) -> DebugNode {
        self.state::<AppState>().get_debug_node(node_id)
    }
}
