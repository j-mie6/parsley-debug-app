use crate::state::{StateError, StateManager, state_manager::BreakpointCode};
use crate::AppState;

#[tauri::command]
pub fn skip_breakpoints(state: tauri::State<'_, AppState>, session_id: i32, skips: i32) -> Result<(), SkipBreakpointError> {
    state.transmit_breakpoint_skips(session_id, BreakpointCode::Skip(skips)).map_err(SkipBreakpointError::from)
}

#[tauri::command]
pub fn skip_all_breakpoints(state: tauri::State<'_, AppState>, session_id: i32) -> Result<(), SkipBreakpointError> {
    state.transmit_breakpoint_skips(session_id, BreakpointCode::SkipAll).map_err(SkipBreakpointError::from)
}

#[tauri::command]
pub fn terminate_debugging(state: tauri::State<'_, AppState>, session_id: i32) -> Result<(), SkipBreakpointError> {
    state.transmit_breakpoint_skips(session_id, BreakpointCode::Terminate).map_err(SkipBreakpointError::from)
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