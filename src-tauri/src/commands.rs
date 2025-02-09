use std::sync::{Mutex, MutexGuard};

use crate::{AppState, DebugNode, DebugTree};


/* Expose command handlers for Tauri setup */
pub fn handlers() -> impl Fn(tauri::ipc::Invoke) -> bool {
    tauri::generate_handler![fetch_debug_tree, fetch_node_children]
}


/* Frontend-accessible debug render */
#[tauri::command]
fn fetch_debug_tree(state: tauri::State<Mutex<AppState>>) -> String {
    /* Acquire the state mutex to access the parser */
    let tree: &Option<DebugTree> = &state.lock().expect("State mutex could not be acquired").tree;
    
    /* If parser exists, render in JSON */
    match tree {
        Some(tree) => serde_json::to_string_pretty(tree)
            .expect("Debug tree could not be serialised"),
        None => String::from(""),
    }
}

/* Backend reactive fetch children */
#[tauri::command]
fn fetch_node_children(state: tauri::State<Mutex<AppState>>, node_id: u32) -> Result<String, FetchChildrenError> {
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
enum FetchChildrenError {
    LockFailed,
    NodeNotFound(u32),
    SerdeError,
}