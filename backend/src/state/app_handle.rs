use tauri::{Emitter, Manager};

use crate::events::Event;
use crate::trees::{DebugNode, DebugTree};
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

    fn transmit_breakpoint_skips(&self, skips: i32) -> Result<(),StateError> {
        self.state::<AppState>().transmit_breakpoint_skips(skips)
    }
    
    fn get_download_path(&self) -> Result<PathBuf, StateError> {
        self.path().download_dir()
            .map_err(|_| StateError::LockFailed)
    }
}