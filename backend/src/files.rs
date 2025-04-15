use std::fs;
use std::path::Path;


/* Directory for saved trees. */
pub const SAVED_TREE_DIR : &str = "./saved_trees/";


/* Removes all saved_trees wiith the folder */
pub fn delete_saved_trees_dir(path_to_dir: &Path) -> Result<(), FileError> {
    fs::remove_dir_all(path_to_dir)
        .map_err(|_| FileError::DeleteDirFailed)
}


pub fn create_saved_trees_dir(path_to_dir: &Path) -> Result<(), FileError> {
    /* If the folder for saved_trees does not exist, create it. */
    if !path_to_dir.exists() {
        fs::create_dir(path_to_dir)
            .map_err(|_| FileError::CreateDirFailed)?;
    }

    Ok(())
}

#[derive (Debug)]
pub enum FileError  {
    DeleteDirFailed,
    CreateDirFailed,
}