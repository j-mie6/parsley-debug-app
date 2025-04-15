use std::collections::HashMap;

use tauri::{Emitter, Manager};

use crate::events::Event;
use crate::trees::{DebugNode, DebugTree};
use crate::state::state_manager::SkipsSender;
use super::state_manager::{DirectoryKind, UpdateTreeError};
use super::{AppState, StateManager, StateError};
use std::path::PathBuf;

/* Wrapper for Tauri AppHandle */
pub struct AppHandle(tauri::AppHandle);

impl AppHandle {
    pub fn new(app_handle: tauri::AppHandle) -> AppHandle {
        AppHandle(app_handle)
    }

    /* Delegate emit to wrapped Tauri AppHandle */
    pub fn emit(&self, event: Event) -> Result<(), StateError> {
        StateManager::emit(&self.0, event)
    }
}


/* Delegate StateManager implementations to AppState accessed via Tauri AppHandle */
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

    /* Emit event using the Tauri AppHandle */
    fn emit<'a>(&self, event: Event<'a>) -> Result<(), StateError> {
        Emitter::emit(self, &event.name(), event.payload()?)
            .map_err(|_| StateError::EventEmitFailed)
    }

    fn transmit_breakpoint_skips(&self, session_id: i32, skips: i32) -> Result<(), StateError> {
        self.state::<AppState>().transmit_breakpoint_skips(session_id, skips)
    }
    
    fn add_session_id(&self, tree_name: String, session_id:i32) -> Result<(), StateError> {
        self.state::<AppState>().add_session_id(tree_name, session_id)
    }
    
    fn rmv_session_id(&self, tree_name: String) -> Result<(), StateError> {
        self.state::<AppState>().rmv_session_id(tree_name)
    }
    
    fn session_id_exists(&self, session_id:i32) -> Result<bool, StateError> {
        self.state::<AppState>().session_id_exists(session_id)
    }

    fn get_session_ids(&self) -> Result<HashMap<String, i32>, StateError> {
        self.state::<AppState>().get_session_ids()
    }

    fn next_session_id(&self) -> Result<i32, StateError> {
        self.state::<AppState>().next_session_id()
    }
    
    fn new_transmitter(&self, session_id: i32, tx: SkipsSender) -> Result<(), StateError> {
        self.state::<AppState>().new_transmitter(session_id, tx)
    }

    fn system_path(&self, dir: DirectoryKind) -> Result<PathBuf, StateError> {
        self.state::<AppState>().system_path(dir)
    }
    
    fn reset_refs(&self, session_id: i32, default_refs: Vec<(i32, String)>) -> Result<(), StateError> {
        self.state::<AppState>().reset_refs(session_id, default_refs)
    }

    fn get_refs(&self, session_id: i32) -> Result<Vec<(i32, String)>, StateError> {
        self.state::<AppState>().get_refs(session_id)
    }

    fn reset_trees(&self) -> Result<(), StateError> {
        self.state::<AppState>().reset_trees()
    }

    fn update_tree(&self, tree: &DebugTree, tree_name: String) -> Result<(), UpdateTreeError> {
        self.state::<AppState>().update_tree(tree, tree_name)
    }
}