use std::fs;

use crate::SAVED_TREE_DIR;


/* Removes all saved_trees wiith the folder */
pub fn clean_saved_trees() -> Result<(), CleanTreesError> {
    fs::remove_dir_all(SAVED_TREE_DIR).map_err(|_| CleanTreesError::CleanTreesError)?;
    Ok(())
}

#[derive (Debug)]
pub enum CleanTreesError {
    CleanTreesError
}