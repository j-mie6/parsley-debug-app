use std::collections::HashMap;

use tauri::{Emitter, Manager};

use crate::events::Event;
use crate::trees::{DebugNode, DebugTree};
use crate::state::state_manager::SkipsSender;
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

    pub fn get_download_path(&self) -> Result<PathBuf, StateError> {
        self.0.get_download_path()
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

    fn transmit_breakpoint_skips(&self, session_id: i32, skips: i32, new_refs: Vec<(i32, String)>) -> Result<(), StateError> {
        self.state::<AppState>().transmit_breakpoint_skips(session_id, skips, new_refs)
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
    
    fn get_download_path(&self) -> Result<PathBuf, StateError> {
        self.path().download_dir()
            .map_err(|_| StateError::GetDownloadPathFail)
    }

    fn reset_trees(&self) -> Result<(), StateError> {
        self.state::<AppState>().reset_trees()
    }
}