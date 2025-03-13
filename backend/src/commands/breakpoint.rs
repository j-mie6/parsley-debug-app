use crate::state::{StateError, StateManager};
use crate::AppState;

#[tauri::command]
pub fn skip_breakpoints(state: tauri::State<'_, AppState>, session_id: i32, skips: i32) -> Result<(), SkipBreakpointError> {
    state.transmit_breakpoint_skips(session_id, skips).map_err(SkipBreakpointError::from)
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

#[tauri::command]
pub fn stop_debugging(state: tauri::State<'_, AppState>, session_id: i32) -> Result<(), StopDebuggingError> {
    state.stop_debugging_session().map_err(StopDebuggingError::from)
}

#[derive(Debug, serde::Serialize)]
pub enum StopDebuggingError {
    LockFailed,
    TreeNotFound,
}

impl From<StateError> for StopDebuggingError {
    fn from(err: StateError) -> Self {
        match err {
            StateError::LockFailed => Self::LockFailed,
            StateError::TreeNotFound => Self::TreeNotFound,
            _ => panic!("Unexpected errror on stop_debuggging")
        }
    }
}
