mod app_state;
mod state_manager;

pub use state_manager::{StateManager, StateError};
#[cfg(test)] pub use state_manager::MockStateManager;

pub use app_state::AppState;