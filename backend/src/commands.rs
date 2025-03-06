mod fetch;
mod save;
mod breakpoint;
mod source;

/* Expose command handlers for Tauri setup */
pub fn handlers() -> impl Fn(tauri::ipc::Invoke) -> bool {
    tauri::generate_handler![
        fetch::fetch_debug_tree, 
        fetch::fetch_node_children, 
        save::save_tree, 
        save::load_saved_tree,
        save::delete_tree,
        breakpoint::skip_breakpoints,
        source::request_source_file
    ]
}
