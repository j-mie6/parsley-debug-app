use serde::Serialize;
use tauri::{Emitter, Manager};

use crate::trees::{DebugTree, DebugNode};
use super::{AppState, StateManager, StateError};


/* Wrapper for Tauri AppHandle */
pub struct AppHandle(tauri::AppHandle);

impl AppHandle {
    pub fn new(app_handle: tauri::AppHandle) -> AppHandle {
        AppHandle(app_handle)
    }

    /* Delegate emit to wrapped Tauri AppHandle */
    pub fn emit<S>(&self, event: &str, payload: S) -> Result<(), StateError>
    where 
        S: Serialize + Clone
    {
        self.0.emit(event, payload)
            .map_err(|_| StateError::EventEmitFailed)
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

    fn transmit_breakpoint_skips(&self, skips: i32) -> Result<(),StateError> {
        self.state::<AppState>().transmit_breakpoint_skips(skips)
    }
}