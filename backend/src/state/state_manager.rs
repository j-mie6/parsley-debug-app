#[cfg(test)] use mockall::automock;

use crate::events::Event;
use crate::trees::{DebugNode, DebugTree};


#[cfg_attr(test, automock)]
pub trait StateManager: Send + Sync + 'static {
    fn set_tree(&self, tree: DebugTree) -> Result<(), StateError>;

    fn get_tree(&self) -> Result<DebugTree, StateError>;

    fn get_node(&self, id: u32) -> Result<DebugNode, StateError>;

    fn emit<'a>(&self, event: Event<'a>) -> Result<(), StateError>;

    fn transmit_breakpoint_skips(&self, session_id: i32, skips: i32) -> Result<(), StateError>;

    fn add_session_id(&self, tree_name: String, session_id: i32) -> Result<(),StateError>;

    fn rmv_session_id(&self, tree_name: String) -> Result<(),StateError>;

    fn session_id_exists(&self, session_id: i32) -> Result<bool,StateError>;

    fn next_session_id(&self) -> Result<i32, StateError>;
}


#[derive(Debug)]
pub enum StateError {
    LockFailed,
    TreeNotFound,
    NodeNotFound(u32),
    EventEmitFailed,
    ChannelError, /* Non-fatal error: The receiver from Parsley is no longer listening */
}