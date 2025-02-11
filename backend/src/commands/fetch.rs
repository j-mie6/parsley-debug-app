use crate::state::StateManager;
use crate::AppState;
use crate::trees::DebugNode;

/* Frontend-accessible debug render */
#[tauri::command]
pub fn fetch_debug_tree(state: tauri::State<AppState>) -> String {
    serde_json::to_string_pretty(&state.get_tree()).expect("Debug tree could not be serialised")
}

/* Backend reactive fetch children */
#[tauri::command]
pub fn fetch_node_children(state: tauri::State<AppState>, node_id: u32) -> Result<String, FetchChildrenError> {
    /* Find node with corresponding node id */
    let node: DebugNode = state.get_debug_node(node_id);

    /* Serialise children */
    serde_json::to_string_pretty(&node.children)
        .map_err(|_| FetchChildrenError::SerdeError)
}

#[derive(Debug, serde::Serialize)]
pub enum FetchChildrenError {
    LockFailed,
    NodeNotFound(u32),
    SerdeError,
}

