use std::collections::HashMap;

#[cfg(test)] use mockall::automock;

use crate::events::Event;
use crate::trees::{DebugNode, DebugTree};

pub type SkipsSender = rocket::tokio::sync::oneshot::Sender<(i32, Vec<(i32, String)>)>;

#[cfg_attr(test, automock)]
pub trait StateManager: Send + Sync + 'static {
    fn set_tree(&self, tree: DebugTree) -> Result<(), StateError>;

    fn get_tree(&self) -> Result<DebugTree, StateError>;

    fn get_node(&self, id: u32) -> Result<DebugNode, StateError>;

    fn emit<'a>(&self, event: Event<'a>) -> Result<(), StateError>;

    fn transmit_breakpoint_skips(&self, session_id: i32, skips: i32, new_refs: Vec<(i32, String)>) -> Result<(), StateError>;

    fn add_session_id(&self, tree_name: String, session_id: i32) -> Result<(), StateError>;

    fn rmv_session_id(&self, tree_name: String) -> Result<(), StateError>;

    fn session_id_exists(&self, session_id: i32) -> Result<bool, StateError>;
    
    fn get_session_ids(&self) -> Result<HashMap<String, i32>, StateError>;

    fn next_session_id(&self) -> Result<i32, StateError>;

    fn new_transmitter(&self, session_id: i32, tx: SkipsSender) -> Result<(), StateError>;
}


#[derive(Debug)]
pub enum StateError {
    LockFailed,
    TreeNotFound,
    NodeNotFound(u32),
    EventEmitFailed,
    ChannelError, /* Non-fatal error: The receiver from Parsley is no longer listening */
}