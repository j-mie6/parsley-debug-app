use crate::{state::{StateError, StateManager}, AppState};

#[derive(Debug, serde::Serialize)]
pub enum SkipBreakpointError {
    EventEmitFailed,
}

impl From<StateError> for SkipBreakpointError {
    fn from(err: StateError) -> Self {
        match err {
            StateError::EventEmitFailed => Self::EventEmitFailed,
            _ => panic!("Unexpected error on skip_breakpoints"),
        }
    }
}

#[tauri::command]
pub fn skip_breakpoints(state: tauri::State<'_, AppState>, skips: i32) -> Result<(), SkipBreakpointError> {
    state.transmit_breakpoint_skips(skips)?;
    Ok(())
}