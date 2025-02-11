use std::sync::{Mutex, MutexGuard};

use crate::{debug_tree::DebugNode, AppState, DebugTree};

/* Frontend-accessible debug render */
#[tauri::command]
pub fn fetch_debug_tree(state: tauri::State<Mutex<AppState>>) -> String {
    /* Acquire the state mutex to access the tree */
    let state_guard: MutexGuard<AppState> = state.lock().expect("State mutex could not be acquired");
    let tree: Option<&DebugTree> = state_guard.get_tree();
    
    /* If parser exists, render in JSON */
    match tree {
        Some(tree) => serde_json::to_string_pretty(tree).expect("Debug tree could not be serialised"),
        None => String::from(""),
    }
}

/* Backend reactive fetch children */
#[tauri::command]
pub fn fetch_node_children(state: tauri::State<Mutex<AppState>>, node_id: u32) -> Result<String, FetchChildrenError> {
    /* Acquire the state mutex to access the corresponding debug node */
    let state_guard: MutexGuard<AppState> = state.lock().map_err(|_| FetchChildrenError::LockFailed)?;

    /* Find node with corresponding node id */
    let node: &DebugNode = state_guard
        .get_debug_node(node_id)
        .ok_or(FetchChildrenError::NodeNotFound(node_id))?;

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

