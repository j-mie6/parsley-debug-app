use std::path::PathBuf;

#[cfg(test)] use mockall::automock;

use crate::events::Event;
use crate::trees::{DebugNode, DebugTree};


#[cfg_attr(test, automock)]
pub trait StateManager: Send + Sync + 'static {
    fn set_tree(&self, tree: DebugTree) -> Result<(), StateError>;

    fn get_tree(&self) -> Result<DebugTree, StateError>;

    fn get_node(&self, id: u32) -> Result<DebugNode, StateError>;

    fn emit<'a>(&self, event: Event<'a>) -> Result<(), StateError>;

    fn transmit_breakpoint_skips(&self, skips: i32, new_refs: Vec<(i32, String)>) -> Result<(), StateError>;

    fn get_download_path(&self) -> Result<PathBuf, StateError>;
}


#[derive(Debug)]
pub enum StateError {
    LockFailed,
    TreeNotFound,
    NodeNotFound(u32),
    EventEmitFailed,
    GetDownloadPathFail,
    ChannelError, /* Non-fatal error: The receiver from Parsley is no longer listening */
}