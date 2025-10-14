use crate::state::{StateError, StateManager};
use crate::AppState;
use crate::server::{PARSLEYDEBUG_SKIP_ALL, PARSLEYDEBUG_TERMINATE};

#[tauri::command]
pub fn skip_breakpoints(state: tauri::State<'_, AppState>, session_id: i32, skips: i32) -> Result<(), SkipBreakpointError> {
    state.transmit_breakpoint_skips(session_id, skips).map_err(SkipBreakpointError::from)
}

#[tauri::command]
pub fn skip_all_breakpoints(state: tauri::State<'_, AppState>, session_id: i32) -> Result<(), SkipBreakpointError> {
    skip_breakpoints(state, session_id, PARSLEYDEBUG_SKIP_ALL)
}

#[tauri::command]
pub fn terminate_debugging(state: tauri::State<'_, AppState>, session_id: i32) -> Result<(), SkipBreakpointError> {
    skip_breakpoints(state, session_id, PARSLEYDEBUG_TERMINATE)
}

#[derive(Debug, serde::Serialize)]
pub enum SkipBreakpointError {
    ChannelError,
}

impl From<StateError> for SkipBreakpointError {
    fn from(err: StateError) -> Self {
        match err {
            StateError::ChannelError => Self::ChannelError,
            e => panic!("Unexpected error on skip_breakpoints: {:?}", e),
        }
    }
}