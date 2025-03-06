use std::fs::{self, File};
use std::io::Write;

use crate::AppState;
use crate::state::{StateError, StateManager};
use crate::trees::{DebugTree, SavedTree};
use crate::files::SAVED_TREE_DIR;


/* Saves current tree to saved_trees/name.json */
#[tauri::command]
pub fn save_tree(state: tauri::State<AppState>, tree_name: String) -> Result<String, SaveTreeError> {
    /* Get DebugTree from current state */
    let debug_tree: DebugTree = state.get_tree()?;

    let is_debugging: bool = debug_tree.is_debugging();

    /* Get the session_id of the current DebugTree */
    let session_id: i32 = debug_tree.get_session_id();

    /* Access the `tree` field from the locked state */
    let saved_tree: SavedTree = SavedTree::from(debug_tree); 

    /* Get the serialised JSON */
    let tree_json: String = serde_json::to_string_pretty(&saved_tree)
        .map_err(|_| SaveTreeError::SerialiseFailed)?;


    /* Create the json file to store the tree */
    let file_path: String = format!("{}{}.json", SAVED_TREE_DIR, tree_name);
    let mut data_file: File = File::create(file_path).map_err(|_| SaveTreeError::CreateDirFailed)?;

    /* Write tree json to the json file */
    data_file.write(tree_json.as_bytes()).map_err(|_| SaveTreeError::WriteTreeFailed)?;

    /* Add new debugging session if the tree has a valid session_id */
    if is_debugging {
        state.add_session_id(tree_name.clone(), session_id).map_err(|_| SaveTreeError::AddSessionFailed)?;
    }
    
    /* Get a list of all saved tree names */
    let tree_names: Vec<String> = state.add_tree(tree_name).map_err(|_| SaveTreeError::AddTreeFailed)?;

    serde_json::to_string_pretty(&tree_names)
        .map_err(|_| SaveTreeError::SerialiseFailed)
}

#[derive(Debug, serde::Serialize)]
pub enum SaveTreeError {
    LockFailed,
    TreeNotFound,
    SerialiseFailed,
    CreateDirFailed,
    WriteTreeFailed,
    AddTreeFailed,
    AddSessionFailed,
}

/* SHOULD be current tree */
/* Updates a saved tree with new breakpoint skips */
#[tauri::command]
pub fn update_tree(state: tauri::State<AppState>, index: usize) -> Result<(), UpdateTreeError> {
    /* Access the `tree` field from the locked state */
    let cur_tree: DebugTree = state.get_tree().map_err(|_| UpdateTreeError::LoadTreeFailed)?;

    /* Get the serialised JSON */
    let tree_json: String = serde_json::to_string_pretty(&cur_tree)
        .map_err(|_| UpdateTreeError::SerialiseFailed)?;

    let tree_name: String = state.get_tree_name(index).map_err(|_| UpdateTreeError::NameRetrievalFail)?;

    /* Open the json file to update the tree */
    /* TODO: look into only updating the extra bits rather than replacing the tree */
    let file_path: String = format!("{}{}.json", SAVED_TREE_DIR, tree_name);
    let mut data_file: File = File::open(file_path).map_err(|_| UpdateTreeError::OpenFileFailed)?;

    /* Write tree json to the json file */
    data_file.write(tree_json.as_bytes()).map_err(|_| UpdateTreeError::WriteTreeFailed)?;

    Ok(())
}

#[derive(Debug, serde::Serialize)]
pub enum UpdateTreeError {
    LoadTreeFailed,
    SerialiseFailed,
    NameRetrievalFail,
    OpenFileFailed,
    WriteTreeFailed,
}

impl From<StateError> for SaveTreeError {
    fn from(state_error: StateError) -> Self {
        match state_error {
            StateError::LockFailed => SaveTreeError::LockFailed,
            StateError::TreeNotFound => SaveTreeError::TreeNotFound,
            _ => panic!("Unexpected error on save_tree"),
        }
    }
}


/* Delete file associated with where tree is saved */
#[tauri::command]
pub fn delete_tree(state: tauri::State<AppState>, index: usize) -> Result<String, DeleteTreeError> {
    /* Get the tree name given the index */
    let tree_name: String = state.get_tree_name(index).map_err(|_| DeleteTreeError::NameRetrievalFail)?;

    /* Path to the json file used to store the tree */
    let file_path: String = format!("{}{}.json", SAVED_TREE_DIR, tree_name);

    /* Remove the file from the file system */
    fs::remove_file(file_path).map_err(|_| DeleteTreeError::TreeFileRemoveFail)?;

    /* Will remove the session map entry if it exists */
    state.rmv_session_id(tree_name).map_err(|_| DeleteTreeError::SessionIdRemovalFail)?;
    
    /* Returns a list of the tree names that are left */
    let tree_names: Vec<String> = state.rmv_tree(index).map_err(|_| DeleteTreeError::TreeRemovalFail)?;

    serde_json::to_string_pretty(&tree_names)
        .map_err(|_| DeleteTreeError::SerialiseFailed)
}

#[derive(Debug, serde::Serialize)]
pub enum DeleteTreeError {
    TreeFileRemoveFail,
    NameRetrievalFail,
    TreeRemovalFail,
    SerialiseFailed,
    SessionIdRemovalFail,
}


/* Fetches a tree from saved_trees and resets the tree in the tauri state */
#[tauri::command]
pub fn load_saved_tree(index: usize, state: tauri::State<AppState>) -> Result<(), LoadTreeError>  {
    /* Get the tree name given the index */
    let tree_name: String = state.get_tree_name(index).map_err(|_| LoadTreeError::NameRetrievalFail)?;

    /* Get the file path of the tree to be reloaded */
    let file_path: String = format!("{}{}.json", SAVED_TREE_DIR, tree_name);
    
    /* Read the contents of the file as a string */
    let contents: String = fs::read_to_string(file_path)
        .map_err(|_| LoadTreeError::ReadFileFailed)?;


    /* Deserialize the tree into SavedTree, then convert to DebugTree */
    let saved_tree: SavedTree = serde_json::from_str(&contents).map_err(|_| LoadTreeError::DeserialiseFailed)?;

    let tree: DebugTree = DebugTree::from(saved_tree);

    /* Update the global tauri state with the reloaded tree */
    state.set_tree(tree)?;

    Ok(())
}

#[derive(Debug, serde::Serialize)]
#[allow(clippy::enum_variant_names)]
pub enum LoadTreeError {
    NameRetrievalFail,
    LockFailed,
    ReadFileFailed,
    DeserialiseFailed,
    EventEmitFailed,
}

impl From<StateError> for LoadTreeError {
    fn from(state_error: StateError) -> Self {
        match state_error {
            StateError::LockFailed => LoadTreeError::LockFailed,
            StateError::EventEmitFailed => LoadTreeError::EventEmitFailed,
            _ => panic!("Unexpected error on load_saved_tree"),
        }
    }
}
