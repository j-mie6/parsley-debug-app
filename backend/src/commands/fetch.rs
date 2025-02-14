use crate::state::{StateError, StateManager};
use crate::AppState;
use crate::trees::DebugNode;


/* Frontend-accessible debug render */
#[tauri::command]
pub fn fetch_debug_tree(state: tauri::State<AppState>) -> Result<String, FetchTreeError> {
    // serde_json::to_string_pretty(&state.get_tree()?)
    //     .map_err(|_| FetchTreeError::SerialiseFailed)
    Err(FetchTreeError::TreeNotFound)
}

#[derive(Debug, serde::Serialize)]
pub enum FetchTreeError {
    LockFailed,
    TreeNotFound,
    SerialiseFailed,
}

impl From<StateError> for FetchTreeError {
    fn from(state_error: StateError) -> Self {
        match state_error {
            StateError::LockFailed => FetchTreeError::LockFailed,
            StateError::TreeNotFound => FetchTreeError::TreeNotFound,
            _ => panic!("Unexpected error on fetch_debug_tree"),
        }
    }
}


/* Backend reactive fetch children */
#[tauri::command]
pub fn fetch_node_children(state: tauri::State<AppState>, node_id: u32) -> Result<String, FetchChildrenError> {
    /* Find node with corresponding node id */
    let node: DebugNode = state.get_node(node_id)?;

    /* Serialise children */
    serde_json::to_string_pretty(&node.children)
        .map_err(|_| FetchChildrenError::SerialiseFailed)
}

#[derive(Debug, serde::Serialize)]
pub enum FetchChildrenError {
    LockFailed,
    NodeNotFound(u32),
    SerialiseFailed,
}

impl From<StateError> for FetchChildrenError {
    fn from(state_error: StateError) -> Self {
        match state_error {
            StateError::LockFailed => FetchChildrenError::LockFailed,
            StateError::NodeNotFound(id) => FetchChildrenError::NodeNotFound(id),
            _ => panic!("Unexpected error on fetch_node_children"),
        }
    }
}
