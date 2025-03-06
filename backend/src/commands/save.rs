use std::fs::{self, File};
use std::io::Write;
use std::path::PathBuf;

use crate::AppState;
use crate::events::Event;
use crate::state::{StateError, StateManager};
use crate::trees::{DebugTree, SavedTree};
use crate::files::SAVED_TREE_DIR;


/* Saves current tree to saved_trees/name.json */
#[tauri::command]
pub fn save_tree(state: tauri::State<AppState>, tree_name: String) -> Result<String, SaveTreeError> {
    /* Access the `tree` field from the locked state */
    let saved_tree: SavedTree = SavedTree::from(state.get_tree()?); 

    /* Get the serialised JSON */
    let tree_json: String = serde_json::to_string_pretty(&saved_tree)
        .map_err(|_| SaveTreeError::SerialiseFailed)?;


    /* Create the json file to store the tree */
    let file_path: String = format!("{}{}.json", SAVED_TREE_DIR, tree_name);
    let mut data_file: File = File::create(file_path).map_err(|_| SaveTreeError::CreateDirFailed)?;

    /* Write tree json to the json file */
    data_file.write(tree_json.as_bytes()).map_err(|_| SaveTreeError::WriteTreeFailed)?;
    
    /* Get a list of all saved tree names */
    let tree_names: Vec<String> = state.add_tree(tree_name).map_err(|_| SaveTreeError::AddTreeFailed)?;

    serde_json::to_string_pretty(&tree_names)
        .map_err(|_| SaveTreeError::SerialiseFailed)
}

/* Downloads the tree into Downloads folder */
#[tauri::command]
pub fn download_tree(tree_name: String, state: tauri::State<AppState>) -> Result<(), SaveTreeError> {
    /* Path to the json file used to store the tree */
    let file_path: String = format!("{}{}.json", SAVED_TREE_DIR, tree_name);

    /* Get path to Downloads folder */
    let mut download_path: PathBuf = state.get_download_path().unwrap();
    download_path.push(format!("{}.json", tree_name));

    /* Creates a file in Downloads and copies data into it */
    File::create(&download_path).map_err(|_| SaveTreeError::CreateDirFailed)?;
    fs::copy(file_path, download_path).map_err(|_| SaveTreeError::DownloadFailed)?;
    
    Ok(())
}

/* Imports JSON file to display a tree */
#[tauri::command]
pub fn import_tree(name: String, contents: String, state: tauri::State<AppState>) -> Result<(), SaveTreeError> {
    /* Path to the json file used to store the tree */
    let app_path: String = format!("{}{}", SAVED_TREE_DIR, &name);

    /* Creates a file in apps local saved tree folders and writes data from external json it */
    let mut imported_tree: File = File::create(&app_path).map_err(|_| SaveTreeError::CreateDirFailed)?;
    imported_tree.write(contents.as_bytes()).map_err(|_| SaveTreeError::ImportFailed)?;

    /* Load tree in the state and emit an event to frontend, passing the new tree */
    load_path(app_path, state.clone()).map_err(|_| SaveTreeError::ImportFailed)?;
    state.emit(Event::NewTree).map_err(|_| SaveTreeError::ImportFailed)
}

#[derive(Debug, serde::Serialize)]
pub enum SaveTreeError {
    LockFailed,
    TreeNotFound,
    SerialiseFailed,
    CreateDirFailed,
    WriteTreeFailed,
    DownloadFailed,
    ImportFailed,
    AddTreeFailed,
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
}


/* Fetches a tree from saved_trees and resets the tree in the tauri state */
#[tauri::command]
pub fn load_saved_tree(state: tauri::State<AppState>, index: usize) -> Result<(), LoadTreeError>  {
    /* Get the tree name given the index */
    let tree_name: String = state.get_tree_name(index).map_err(|_| LoadTreeError::NameRetrievalFail)?;

    /* Get the file path of the tree to be reloaded */
    let file_path: String = format!("{}{}.json", SAVED_TREE_DIR, tree_name);
    load_path(file_path, state)?;
    Ok(())
}
    
/* Loads a tree from the specified file path */
pub fn load_path(file_path: String, state: tauri::State<AppState>) -> Result<(), LoadTreeError> {
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
