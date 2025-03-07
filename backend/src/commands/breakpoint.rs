use crate::state::{StateError, StateManager};
use crate::AppState;

#[tauri::command]
pub fn skip_breakpoints(state: tauri::State<'_, AppState>, skips: i32, new_refs: Vec<(i32, String)>) -> Result<(), SkipBreakpointError> {
    state.transmit_breakpoint_skips(skips, new_refs).map_err(SkipBreakpointError::from)
}

#[derive(Debug, serde::Serialize)]
pub enum SkipBreakpointError {
    ChannelError,
}

impl From<StateError> for SkipBreakpointError {
    fn from(err: StateError) -> Self {
        match err {
            StateError::ChannelError => Self::ChannelError,
            _ => panic!("Unexpected error on skip_breakpoints"),
        }
    }
}