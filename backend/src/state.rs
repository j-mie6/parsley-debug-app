mod app_state;
pub mod state_manager;
mod app_handle;
mod session_counter;

pub use app_state::AppState;
pub use app_handle::AppHandle;

pub use state_manager::{StateManager, StateError};
#[cfg(test)] pub use state_manager::MockStateManager;
