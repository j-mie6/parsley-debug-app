use std::path::PathBuf;

use ambassador::delegatable_trait;
#[cfg(test)] use mockall::automock;

use crate::events::Event;
use crate::trees::{DebugNode, DebugTree};
use crate::server::SkipsSender;

pub enum BreakpointCode {
    Skip(i32),
    SkipAll,
    Terminate,
}

impl BreakpointCode {
    pub fn i32_code(&self) -> i32 {
        match self {
            BreakpointCode::Skip(skips) => *skips,
            BreakpointCode::Terminate => -1,
            BreakpointCode::SkipAll => -2,
        }
    }
}

#[delegatable_trait]
#[cfg_attr(test, automock)]
pub trait StateManager: Send + Sync + 'static {
    fn set_tree(&self, tree: DebugTree) -> Result<(), StateError>;

    fn get_tree(&self) -> Result<DebugTree, StateError>;

    fn get_node(&self, id: u32) -> Result<DebugNode, StateError>;

    fn emit<'a>(&self, event: Event<'a>) -> Result<(), StateError>;

    fn transmit_breakpoint_skips(&self, session_id: i32, code: BreakpointCode) -> Result<(), StateError>;

    fn system_path(&self, dir: DirectoryKind) -> Result<PathBuf, StateError>;

    fn system_path_to(&self, dir: DirectoryKind, path: PathBuf) -> Result<PathBuf, StateError> {
        if path.is_absolute() {
            return Err(StateError::AbsolutePathNotAllowed);
        }
        self.system_path(dir).map(|base| base.join(path))
    }

    // fn add_session_id(&self, tree_name: String, session_id: i32) -> Result<(), StateError>;

    fn rmv_tab(&self, index: usize) -> Result<Vec<String>, StateError>;

    fn get_tab(&self, index: usize) -> Result<(i32, String), StateError>;
    
    fn debuggable_session_ids(&self) -> Result<Vec<i32>, StateError>;

    fn next_session_id(&self) -> Result<i32, StateError>;

    fn new_transmitter(&self, session_id: i32, tx: SkipsSender) -> Result<(), StateError>;

    fn reset_refs(&self, session_id: i32, default_refs: Vec<(i32, String)>) -> Result<(), StateError>;

    fn get_refs(&self, session_id: i32) -> Result<Vec<(i32, String)>, StateError>;

    fn reset_trees(&self) -> Result<(), StateError>;

    /* Updates a saved tree with new breakpoint skips */
    fn update_tree(&self, tree: &DebugTree, session_id: i32) -> Result<(), UpdateTreeError>;
}

#[derive(Debug)]
pub enum DirectoryKind {
    SavedTrees,
    Downloads,
}

#[derive(Debug, serde::Serialize)]
pub enum UpdateTreeError {
    SerialiseFailed,
    OpenFileFailed,
    WriteTreeFailed,
}

#[derive(Debug)]
pub enum StateError {
    LockFailed,
    TreeNotFound,
    NodeNotFound(u32),
    EventEmitFailed,
    AbsolutePathNotAllowed, /* A function was given an unexpected absolute path */
    GetTempdirPathFail,
    GetDownloadPathFail,
    ChannelError, /* Non-fatal error: The receiver from Parsley is no longer listening */
    DuplicateSessionId,
    TabOutOfBounds,
}