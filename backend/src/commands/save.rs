use std::fs::{self, File};
use std::io::Write;
use std::path::PathBuf;

use crate::AppState;
use crate::state::{StateError, StateManager};
use crate::trees::{DebugTree, SavedTree};
use crate::files::SAVED_TREE_DIR;


/* Saves current tree to saved_trees/name.json */
#[tauri::command]
pub fn save_tree(state: tauri::State<AppState>, tree_name: String) -> Result<(), SaveTreeError> {
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

    Ok(())
}

/* Saves current tree to saved_trees/name.json */
#[tauri::command]
pub fn download_tree(state: tauri::State<AppState>, tree_name: String) -> Result<(), SaveTreeError> {
    /* Path to the json file used to store the tree */
    let file_path: String = format!("{}{}.json", SAVED_TREE_DIR, tree_name);

    /* Get path to Downloads folder */
    let mut download_path: PathBuf = state.get_download_path().unwrap();
    download_path.push(format!("{}.json", tree_name));

    /* Creates a file in Downloads and copies data into it */
    let _ = File::create(&download_path).map_err(|_| SaveTreeError::DownloadFailed)?;
    fs::copy(file_path, download_path).map_err(|_| SaveTreeError::DownloadFailed)?;
    
    Ok(())
}

#[derive(Debug, serde::Serialize)]
pub enum SaveTreeError {
    LockFailed,
    TreeNotFound,
    SerialiseFailed,
    CreateDirFailed,
    WriteTreeFailed,
    DownloadFailed,
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
pub fn delete_tree(tree_name: String) -> Result<(), DeleteTreeError> {
    /* Path to the json file used to store the tree */
    let file_path: String = format!("{}{}.json", SAVED_TREE_DIR, tree_name);

    /* Remove the file from the file system */
    fs::remove_file(file_path).map_err(|_| DeleteTreeError::TreeFileRemoveFail)?;
    Ok(())
}

#[derive(Debug, serde::Serialize)]
pub enum DeleteTreeError {
    TreeFileRemoveFail,
}


/* Returns a list of filenames in saved_trees */
#[tauri::command]
pub fn fetch_saved_tree_names() -> Result<String, FetchTreeNameError>  {
    /* Get all path names inside the saved_trees folder */
    let paths: fs::ReadDir = fs::read_dir(SAVED_TREE_DIR).map_err(|_| FetchTreeNameError::ReadDirFailed)?;

    /* Strip off the extension and add only the name to names */
    let names: Vec<String> = paths.into_iter()
        .map(|path| {
            let path: fs::DirEntry = path.map_err(|_| FetchTreeNameError::ReadPathFailed)?;
            let file_name: String = path.file_name().into_string().map_err(|_| FetchTreeNameError::StringContainsInvalidUnicode)?;
            let name: &str = file_name.strip_suffix(".json").ok_or(FetchTreeNameError::SuffixNotFound)?;
            Ok(name.to_string())
        }).collect::<Result<Vec<String>, FetchTreeNameError>>()?;

    /* Serialise names */
    serde_json::to_string_pretty(&names)
        .map_err(|_| FetchTreeNameError::SerialiseFailed)
}

#[derive(Debug, serde::Serialize)]
pub enum FetchTreeNameError {
    ReadDirFailed,
    ReadPathFailed,
    StringContainsInvalidUnicode,
    SuffixNotFound,
    SerialiseFailed,
}


/* Fetches a tree from saved_trees and resets the tree in the tauri state */
#[tauri::command]
pub fn load_saved_tree(tree_name: String, state: tauri::State<AppState>) -> Result<(), LoadTreeError>  {
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
