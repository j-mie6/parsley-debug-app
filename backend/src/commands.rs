mod fetch;
mod save;
mod breakpoint;

/* Expose command handlers for Tauri setup */
pub fn handlers() -> impl Fn(tauri::ipc::Invoke) -> bool {
    tauri::generate_handler![
        fetch::fetch_debug_tree, 
        fetch::fetch_node_children, 
        save::save_tree, 
        save::fetch_saved_tree_names, 
        save::load_saved_tree,
        save::delete_tree,
        save::download_tree,
        breakpoint::skip_breakpoints,
        save::import_tree
    ]
}
