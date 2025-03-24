use std::fs;
use std::path::{Path, PathBuf};


/* Directory for saved trees. */
pub const SAVED_TREE_DIR : &str = "./saved_trees/";


/* Removes all saved_trees wiith the folder */
pub fn delete_saved_trees_dir(mut app_local: PathBuf) -> Result<(), FileError> {
    app_local.push(SAVED_TREE_DIR);

    fs::remove_dir_all(app_local.into_os_string())
        .map_err(|_| FileError::DeleteDirFailed)
}


pub fn create_saved_trees_dir(mut app_local: PathBuf) -> Result<(), FileError> {
    app_local.push(SAVED_TREE_DIR);
    let path: &Path = app_local.as_path();
    
    /* If the folder for saved_trees does not exist, create it. */
    if !path.exists() {
        fs::create_dir(path)
            .map_err(|_| FileError::CreateDirFailed)?;
    }

    Ok(())
}

#[derive (Debug)]
pub enum FileError  {
    DeleteDirFailed,
    CreateDirFailed,
}