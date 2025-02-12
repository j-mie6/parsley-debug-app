mod fetch;
mod save;

/* Expose command handlers for Tauri setup */
pub fn handlers() -> impl Fn(tauri::ipc::Invoke) -> bool {
    tauri::generate_handler![
        fetch::fetch_debug_tree, 
        fetch::fetch_node_children, 
        save::save_tree, 
        save::fetch_saved_tree_names, 
        save::load_saved_tree
    ]
}
