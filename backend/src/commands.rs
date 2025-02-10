use std::io::Write;
use std::sync::{Mutex, MutexGuard};
use std::fs::{self, File};

use crate::SAVED_TREE_DIR;
use crate::{AppState, DebugNode, DebugTree, saved_tree::SavedTree};


/* Expose command handlers for Tauri setup */
pub fn handlers() -> impl Fn(tauri::ipc::Invoke) -> bool {
    tauri::generate_handler![
        fetch_debug_tree, 
        fetch_node_children, 
        save_tree, 
        fetch_saved_tree_names, 
        load_saved_tree
    ]
}


/* Fetching trees */

/* Frontend-accessible debug render */
#[tauri::command]
fn fetch_debug_tree(state: tauri::State<Mutex<AppState>>) -> String {
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


/* Saving trees */

/* Saves current tree to saved_trees/name.json */
#[tauri::command]
fn save_tree(state: tauri::State<Mutex<AppState>>, name: String) -> Result<(), SaveTreeError> {
    /* Acquire the state mutex to access the parser */
    let guard: MutexGuard<AppState> = state.lock().map_err(|_| SaveTreeError::LockFailed)?; // Use `map_err` to convert the error
    let tree: Option<&DebugTree> = guard.get_tree(); // Access the `tree` field from the locked state
    
    /* If tree exists, get the JSON equivalent */
    let tree_json: String = match tree {
        Some(tree) => {
            serde_json::to_string_pretty(&SavedTree::from(tree))
                .map_err(|_| SaveTreeError::SerdeError)?
        },
            
        None => Err(SaveTreeError::NoTreeError)?,
    };


    /* Create the json file to store the tree */
    let file_path: String = format!("saved_trees/{}.json", name);
    let mut data_file: File = File::create(file_path).map_err(|_| SaveTreeError::CreateError)?;

    /* Write tree json to the json file */
    data_file.write(tree_json.as_bytes()).map_err(|_| SaveTreeError::WriteError)?;

    Ok(())
}

#[derive(Debug, serde::Serialize)]
enum SaveTreeError {
    LockFailed,
    NoTreeError,
    SerdeError,
    CreateError,
    WriteError,
}

/* Returns a list of filenames in saved_trees */
#[tauri::command]
fn fetch_saved_tree_names() -> Result<String, TreeSaveError>  {
    /* Get all path names inside the saved_trees folder */
    let paths: fs::ReadDir = fs::read_dir(SAVED_TREE_DIR).unwrap();

    /* Strip off the extension and add only the name to names */
    let names: Vec<String> = paths.into_iter()
        .map(|path| {
            let path: fs::DirEntry = path.map_err(|_| TreeSaveError::ReadPathError)?;
            let file_name: String = path.file_name().into_string().map_err(|_| TreeSaveError::IntoStringError)?;
            let name: &str = file_name.strip_suffix(".json").ok_or(TreeSaveError::StripSuffixError)?;
            Ok(name.to_string())
        }).collect::<Result<Vec<String>, TreeSaveError>>()?;

    /* Serialise names */
    serde_json::to_string_pretty(&names)
        .map_err(|_| TreeSaveError::SerdeError)
}

#[derive(Debug, serde::Serialize)]
enum TreeSaveError {
    ReadPathError,
    IntoStringError,
    StripSuffixError,
    SerdeError,
}

/* Fetches a tree from saved_trees and resets the tree in the tauri state */
#[tauri::command]
fn load_saved_tree(state: tauri::State<Mutex<AppState>>, name: String) -> Result<(), ReloadTreeError>  {
    /* Get the file path of the tree to be reloaded */
    let file_path: String = format!("{}{}.json", SAVED_TREE_DIR, name);

    /* Read the contents of the file as a string */
    let contents: String = fs::read_to_string(file_path)
        .map_err(|_| ReloadTreeError::ReadFileError)?;

    /* Deserialize the tree into SavedTree, then convert to DebugTree */
    let saved_tree: SavedTree = serde_json::from_str(&contents).map_err(|_| ReloadTreeError::SerdeError)?;
    let tree: DebugTree = DebugTree::from(&saved_tree);

    /* Update the global tauri state with the reloaded tree */
    state
        .lock()
        .map_err(|_| ReloadTreeError::LockingError)?
        .set_tree(tree);

    Ok(())
}

#[derive(Debug, serde::Serialize)]
enum ReloadTreeError {
    ReadFileError,
    SerdeError,
    LockingError,
}
