use std::fs;


/* Directory for saved trees. */
pub const SAVED_TREE_DIR : &str = "./saved_trees/";


/* Removes all saved_trees wiith the folder */
pub fn clean_saved_trees() -> Result<(), CleanTreesError> {
    fs::remove_dir_all(SAVED_TREE_DIR).map_err(|_| CleanTreesError::CleanTreesError)?;
    Ok(())
}

#[derive (Debug)]
pub enum CleanTreesError {
    CleanTreesError
}

pub fn make_saved_trees() -> Result<(), MakeSavedTreesError> {
    /* If the folder for saved_trees does not exist, create it. */
    if !std::path::Path::new(SAVED_TREE_DIR).exists() {
        std::fs::create_dir(SAVED_TREE_DIR).map_err(|_| MakeSavedTreesError::MakeSavedTreesError)?;
    }

    Ok(())
}

#[derive (Debug)]
pub enum MakeSavedTreesError {
    MakeSavedTreesError
}