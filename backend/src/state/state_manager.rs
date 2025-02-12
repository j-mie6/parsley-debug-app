#[cfg(test)] use mockall::automock;

use crate::trees::{DebugTree, DebugNode};


#[cfg_attr(test, automock)]
pub trait StateManager: Send + Sync + 'static {
    fn set_tree(&self, tree: DebugTree) -> Result<(), StateError>;
    
    fn get_tree(&self) -> Result<DebugTree, StateError>;
    
    fn get_node(&self, id: u32) -> Result<DebugNode, StateError>;
}


#[derive(Debug)]
pub enum StateError {
    LockFailed,
    TreeNotFound,
    NodeNotFound(u32),
    EventEmitFailed,
}