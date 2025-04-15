use std::ffi::OsString;
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
    /* Get DebugTree from current state */
    let debug_tree: DebugTree = state.get_tree()?;

    let is_debuggable: bool = debug_tree.is_debuggable();

    /* Get the session_id of the current DebugTree */
    let session_id: i32 = debug_tree.get_session_id();

    /* Access the `tree` field from the locked state */
    let saved_tree: SavedTree = SavedTree::from(debug_tree);

    /* Get the serialised JSON */
    let tree_json: String = serde_json::to_string_pretty(&saved_tree)
        .map_err(|_| SaveTreeError::SerialiseFailed)?;

    /* Create the json file to store the tree */
    let file_path: OsString = format_filepath(&state, &tree_name)?;
    let mut data_file: File = File::create(file_path).map_err(|_| SaveTreeError::CreateDirFailed)?;

    /* Write tree json to the json file */
    data_file.write(tree_json.as_bytes()).map_err(|_| SaveTreeError::WriteToFileFailed)?;

    /* Add new debugging session if the tree has a valid session_id */
    if is_debuggable {
        state.add_session_id(tree_name.clone(), session_id).map_err(|_| SaveTreeError::AddSessionFailed)?;
    }

    /* Get a list of all saved tree names */
    let tree_names: Vec<String> = state.add_tree(tree_name)?;

    serde_json::to_string_pretty(&tree_names)
        .map_err(|_| SaveTreeError::SerialiseFailed)
}

#[derive(Debug, serde::Serialize)]
pub enum SaveTreeError {
    LockFailed,
    TreeNotFound,
    SerialiseFailed,
    CreateDirFailed,
    WriteToFileFailed,
    AddSessionFailed,
}

impl From<StateError> for SaveTreeError {
    fn from(state_error: StateError) -> Self {
        match state_error {
            StateError::LockFailed => SaveTreeError::LockFailed,
            StateError::TreeNotFound => SaveTreeError::TreeNotFound,
            e => panic!("Unexpected error on save_tree: {:?}", e),
        }
    }
}


/* Downloads the tree into Downloads folder */
#[tauri::command]
pub fn download_tree(state: tauri::State<AppState>, index: usize) -> Result<(), DownloadTreeError> {
    /* Get tree name from index */
    let tree_name: String = state.get_tree_name(index)?;

    /* Path to the json file used to store the tree */
    let file_path: OsString = format_filepath(&state, &tree_name)?;

    /* Get path to Downloads folder */
    let mut download_path: PathBuf = state.get_download_path()?;
    download_path.push(format!("{}.json", tree_name));

    /* Creates a file in Downloads and copies data into it */
    File::create(&download_path).map_err(|_| DownloadTreeError::CreateDirFailed)?;
    fs::copy(file_path, download_path).map_err(|_| DownloadTreeError::WriteToFileFailed)?;

    Ok(())
}

#[derive(Debug, serde::Serialize)]
pub enum DownloadTreeError {
    DownloadPathNotFound,
    CreateDirFailed,
    WriteToFileFailed,
}

impl From<StateError> for DownloadTreeError {
    fn from(state_error: StateError) -> Self {
        match state_error {
            StateError::LockFailed => DownloadTreeError::DownloadPathNotFound,
            StateError::TreeNotFound => DownloadTreeError::WriteToFileFailed,
            e => panic!("Unexpected error on save_tree: {:?}", e),
        }
    }
}


/* Imports JSON file to display a tree */
#[tauri::command]
pub fn import_tree(tree_name: String, contents: String, state: tauri::State<AppState>) -> Result<(), ImportTreeError> {
    /* Path to the json file used to store the tree */
    let file_path: OsString = format_filepath(&state, &tree_name)?;

    /* Creates a file in apps local saved tree folders and writes data from external json it */
    let mut imported_tree: File = File::create(&file_path).map_err(|_| ImportTreeError::CreateDirFailed)?;
    imported_tree.write(contents.as_bytes()).map_err(|_| ImportTreeError::WriteToFileFailed)?;

    /* Load tree in the state and emit an event to frontend, passing the new tree */
    load_path(file_path, true, &state)?;
    state.emit(Event::NewTree).map_err(ImportTreeError::from)
}

#[derive(Debug, serde::Serialize)]
pub enum ImportTreeError {
    CreateDirFailed,
    WriteToFileFailed,
    DeserialiseFailed,
    EventEmitFailed,
    LockFailed,
    ReadFileFailed,
}

impl From<StateError> for ImportTreeError {
    fn from(state_error: StateError) -> Self {
        match state_error {
            StateError::LockFailed => ImportTreeError::LockFailed,
            StateError::EventEmitFailed => ImportTreeError::EventEmitFailed,
            StateError::TreeNotFound => ImportTreeError::WriteToFileFailed,
            e => panic!("Unexpected error on save_tree: {:?}", e),
        }
    }
}

impl From<LoadTreeError> for ImportTreeError {
    fn from(load_error: LoadTreeError) -> Self {
        match load_error {
            LoadTreeError::LockFailed => ImportTreeError::LockFailed,
            LoadTreeError::ReadFileFailed => ImportTreeError::ReadFileFailed,
            LoadTreeError::DeserialiseFailed => ImportTreeError::DeserialiseFailed,
            LoadTreeError::EventEmitFailed => ImportTreeError::EventEmitFailed,
        }
    }
}

/* Delete file associated with where tree is saved */
#[tauri::command]
pub fn delete_tree(state: tauri::State<AppState>, index: usize) -> Result<String, DeleteTreeError> {
    /* Get the tree name given the index */
    let tree_name: String = state.get_tree_name(index).map_err(|_| DeleteTreeError::NameRetrievalFail)?;

    /* Path to the json file used to store the tree */
    let file_path: OsString = format_filepath(&state, &tree_name)?;

    /* Remove the file from the file system */
    fs::remove_file(file_path).map_err(|_| DeleteTreeError::TreeFileRemoveFail)?;

    /* Will remove the session map entry and saved refs if they exist */
    state.rmv_session_id(tree_name).map_err(|_| DeleteTreeError::SessionIdRemovalFail)?;

    /* Returns a list of the tree names that are left */
    let tree_names: Vec<String> = state.rmv_tree(index).map_err(|_| DeleteTreeError::TreeRemovalFail)?;

    serde_json::to_string_pretty(&tree_names)
        .map_err(|_| DeleteTreeError::SerialiseFailed)
}

/* Deletes all saved trees */
#[tauri::command]
pub fn delete_saved_trees(state: tauri::State<AppState>) -> Result<(), DeleteTreeError> {
    state.reset_trees()?;

    let path_to_saved_trees: PathBuf = state.app_path_to(PathBuf::from(SAVED_TREE_DIR))?;

    fs::remove_dir_all(path_to_saved_trees.clone()).map_err(|_| DeleteTreeError::TreeFileRemoveFail)?;
    fs::create_dir(path_to_saved_trees).map_err(|_| DeleteTreeError::FolderCreationFail)
}

#[derive(Debug, serde::Serialize)]
pub enum DeleteTreeError {
    TreeFileRemoveFail,
    NameRetrievalFail,
    TreeRemovalFail,
    SerialiseFailed,
    SessionIdRemovalFail,
    FolderCreationFail,
    TreesResetFailed,
}

impl From<StateError> for DeleteTreeError {
    fn from(_: StateError) -> Self {
        DeleteTreeError::TreesResetFailed
    }
}


/* Fetches a tree from saved_trees and resets the tree in the tauri state */
#[tauri::command]
pub fn load_saved_tree(index: usize, state: tauri::State<AppState>) -> Result<(), LoadTreeError>  {
    /* Get the tree name given the index */
    let tree_name: String = state.get_tree_name(index)?;

    /* Get the file path of the tree to be reloaded */
    let file_path: OsString = format_filepath(&state, &tree_name)?;
    load_path(file_path, false, &state)
}

/* Loads a tree from the specified file path */
fn load_path(file_path: OsString, is_import: bool, state: &tauri::State<AppState>) -> Result<(), LoadTreeError> {
    /* Read the contents of the file as a string */
    let contents: String = fs::read_to_string(file_path)
        .map_err(|_| LoadTreeError::ReadFileFailed)?;


    /* Deserialize the tree into SavedTree, then convert to DebugTree */
    let saved_tree: SavedTree = serde_json::from_str(&contents).map_err(|_| LoadTreeError::DeserialiseFailed)?;

    let mut tree: DebugTree = DebugTree::from(saved_tree);

    /* If we are importing the tree, we must turn debugging off and give it a new session_id */
    if is_import {
        tree.set_is_debugging(false);
        let session_id: i32 = state.next_session_id()?;
        tree.set_session_id(session_id);
    }

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
            e => panic!("Unexpected error on load_saved_tree: {:?}", e),
        }
    }
}

/* Generates the full path to a tree file in the form `APPDATA/SAVED_TREE_DIR/tree_name` */
fn format_filepath(state: &tauri::State<AppState>, tree_name: &str) -> Result<OsString, StateError> {
    let mut path_to_tree = PathBuf::new();
    path_to_tree.push(SAVED_TREE_DIR);
    path_to_tree.push(tree_name);

    let full_path = state.app_path_to(path_to_tree)?;
    Ok(full_path.into_os_string())
}

/* Updates local changed references for a tree */
#[tauri::command]
pub fn update_refs(new_refs: Vec<(i32, String)>, state: tauri::State<AppState>) -> Result<(), RefError>  {
    let session_id: i32 = state.get_tree()?.get_session_id();

    Ok(state.update_refs(session_id, new_refs)?)
}

/* Retrieves local changed references for a tree */
#[tauri::command]
pub fn get_refs(session_id: i32, state: tauri::State<AppState>) -> Result<String, RefError>  {
    let refs: Vec<(i32, String)> = state.get_refs(session_id)?;

    serde_json::to_string_pretty(&refs)
        .map_err(|_| RefError::RefMapFail)
}

/* Resets local changes to default for a tree's refs */
#[tauri::command]
pub fn reset_refs(state: tauri::State<AppState>) -> Result<String, RefError>  {
    let debug_tree: DebugTree = state.get_tree()?;

    let session_id: i32 = debug_tree.get_session_id();

    let default_refs: Vec<(i32, String)> = debug_tree.refs();
    state.reset_refs(session_id, default_refs.clone())?;

    serde_json::to_string_pretty(&default_refs)
        .map_err(|_| RefError::RefMapFail)
}

#[derive(Debug, serde::Serialize)]
#[allow(clippy::enum_variant_names)]
pub enum RefError {
    RefMapFail,
}

impl From<StateError> for RefError {
    fn from(state_error: StateError) -> Self {
        match state_error {
            StateError::LockFailed => RefError::RefMapFail,
            e => panic!("Unexpected error on load_saved_tree: {:?}", e),
        }
    }
}
