use std::io::Write;
use std::fs::{self, File};

use crate::state::StateManager;
use crate::{AppState, SAVED_TREE_DIR};
use crate::trees::{DebugTree, SavedTree};


/* Saves current tree to saved_trees/name.json */
#[tauri::command]
pub fn save_tree(state: tauri::State<AppState>, name: String) -> Result<(), SaveTreeError> {
    /* Access the `tree` field from the locked state */
    let saved_tree: SavedTree = SavedTree::from(&state.get_tree()); 
    
    /* Get the serialised JSON */
    let tree_json: String = serde_json::to_string_pretty(&saved_tree)
        .map_err(|_| SaveTreeError::SerdeError)?;


    /* Create the json file to store the tree */
    let file_path: String = format!("saved_trees/{}.json", name);
    let mut data_file: File = File::create(file_path).map_err(|_| SaveTreeError::CreateError)?;

    /* Write tree json to the json file */
    data_file.write(tree_json.as_bytes()).map_err(|_| SaveTreeError::WriteError)?;

    Ok(())
}

#[derive(Debug, serde::Serialize)]
pub enum SaveTreeError {
    LockFailed,
    NoTreeError,
    SerdeError,
    CreateError,
    WriteError,
}


/* Returns a list of filenames in saved_trees */
#[tauri::command]
pub fn fetch_saved_tree_names() -> Result<String, TreeSaveError>  {
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
pub enum TreeSaveError {
    ReadPathError,
    IntoStringError,
    StripSuffixError,
    SerdeError,
}


/* Fetches a tree from saved_trees and resets the tree in the tauri state */
#[tauri::command]
pub fn load_saved_tree(state: tauri::State<AppState>, name: String) -> Result<(), ReloadTreeError>  {
    /* Get the file path of the tree to be reloaded */
    let file_path: String = format!("{}{}.json", SAVED_TREE_DIR, name);

    /* Read the contents of the file as a string */
    let contents: String = fs::read_to_string(file_path)
        .map_err(|_| ReloadTreeError::ReadFileError)?;

    /* Deserialize the tree into SavedTree, then convert to DebugTree */
    let saved_tree: SavedTree = serde_json::from_str(&contents).map_err(|_| ReloadTreeError::SerdeError)?;
    let tree: DebugTree = DebugTree::from(&saved_tree);

    /* Update the global tauri state with the reloaded tree */
    state.set_tree(tree);

    Ok(())
}

#[derive(Debug, serde::Serialize)]
pub enum ReloadTreeError {
    ReadFileError,
    SerdeError,
    LockingError,
}
