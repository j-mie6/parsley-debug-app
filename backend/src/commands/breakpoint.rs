use crate::state::{StateError, StateManager};
use crate::AppState;

#[tauri::command]
pub fn skip_breakpoints(state: tauri::State<'_, AppState>, skips: i32) -> Result<(), SkipBreakpointError> {
    let dummy: Vec<(i32, String)> = Vec::new(); // TODO: Get as a parameter
    state.transmit_breakpoint_skips(skips, dummy).map_err(SkipBreakpointError::from)
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