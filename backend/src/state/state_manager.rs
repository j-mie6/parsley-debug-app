use std::path::PathBuf;

use std::collections::HashMap;

#[cfg(test)] use mockall::automock;

use crate::events::Event;
use crate::trees::{DebugNode, DebugTree};

pub type SkipsSender = rocket::tokio::sync::oneshot::Sender<i32>;

#[cfg_attr(test, automock)]
pub trait StateManager: Send + Sync + 'static {
    fn set_tree(&self, tree: DebugTree) -> Result<(), StateError>;

    fn get_tree(&self) -> Result<DebugTree, StateError>;

    fn get_node(&self, id: u32) -> Result<DebugNode, StateError>;

    fn emit<'a>(&self, event: Event<'a>) -> Result<(), StateError>;

    fn transmit_breakpoint_skips(&self, session_id: i32, skips: i32) -> Result<(), StateError>;

    fn get_app_localdata_path(&self) -> Result<PathBuf, StateError>;

    /** Returns the given PathBuf prefixed with `/path/to/app_localdata`. The given path should be a relative path. */
    fn app_path_to(&self, path: PathBuf) -> Result<PathBuf, StateError> {
        if path.is_absolute() {
            return Err(StateError::AbsolutePathNotAllowed);
        }
        self.get_app_localdata_path().map(|app_local| app_local.join(path))
    }

    fn get_download_path(&self) -> Result<PathBuf, StateError>;

    fn add_session_id(&self, tree_name: String, session_id: i32) -> Result<(), StateError>;

    fn rmv_session_id(&self, tree_name: String) -> Result<(), StateError>;

    fn session_id_exists(&self, session_id: i32) -> Result<bool, StateError>;
    
    fn get_session_ids(&self) -> Result<HashMap<String, i32>, StateError>;

    fn next_session_id(&self) -> Result<i32, StateError>;

    fn new_transmitter(&self, session_id: i32, tx: SkipsSender) -> Result<(), StateError>;

    fn reset_refs(&self, session_id: i32, default_refs: Vec<(i32, String)>) -> Result<(), StateError>;

    fn get_refs(&self, session_id: i32) -> Result<Vec<(i32, String)>, StateError>;

    fn reset_trees(&self) -> Result<(), StateError>;
}


#[derive(Debug)]
pub enum StateError {
    LockFailed,
    TreeNotFound,
    NodeNotFound(u32),
    EventEmitFailed,
    AbsolutePathNotAllowed, /* A function was given an unexpected absolute path */
    GetAppLocalDataPathFail,
    GetDownloadPathFail,
    ChannelError, /* Non-fatal error: The receiver from Parsley is no longer listening */
}