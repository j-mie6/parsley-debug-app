use tauri::Manager;
#[cfg(test)] use mockall::automock;

use super::AppState;
use crate::trees::{DebugTree, DebugNode};


#[cfg_attr(test, automock)]
pub trait StateManager: Send + Sync + 'static {
    fn set_tree(&self, tree: DebugTree) -> Result<(), StateError>;

    fn get_tree(&self) -> Result<DebugTree, StateError>;

    fn get_node(&self, id: u32) -> Result<DebugNode, StateError>;
}


impl StateManager for tauri::AppHandle {
    fn set_tree(&self, tree: DebugTree) -> Result<(), StateError> {
        self.state::<AppState>().set_tree(tree)
    }

    fn get_tree(&self) -> Result<DebugTree, StateError> {
        self.state::<AppState>().get_tree()
    }

    fn get_node(&self, id: u32) -> Result<DebugNode, StateError> {
        self.state::<AppState>().get_node(id)
    }
}

#[derive(Debug)]
pub enum StateError {
    LockFailed,
    TreeNotFound,
    NodeNotFound(u32),
    EventEmitFailed,
}