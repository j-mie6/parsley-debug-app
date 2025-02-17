use std::fs;


/* Directory for saved trees. */
pub const SAVED_TREE_DIR : &str = "./saved_trees/";


/* Removes all saved_trees wiith the folder */
pub fn delete_saved_trees() -> Result<(), FileError> {
    fs::remove_dir_all(SAVED_TREE_DIR)
        .map_err(|_| FileError::DeleteDirFailed)?;

    Ok(())
}


pub fn make_saved_trees() -> Result<(), FileError> {
    /* If the folder for saved_trees does not exist, create it. */
    if !std::path::Path::new(SAVED_TREE_DIR).exists() {
        fs::create_dir(SAVED_TREE_DIR)
            .map_err(|_| FileError::CreateDirFailed)?;
    }

    Ok(())
}

#[derive (Debug)]
pub enum FileError  {
    DeleteDirFailed,
    CreateDirFailed,
}