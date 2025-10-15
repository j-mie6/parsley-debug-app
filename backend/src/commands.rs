mod fetch;
pub mod save;
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
        save::download_tree,
        save::import_tree,
        save::get_refs,
        save::update_refs,
        save::reset_refs,
        save::delete_saved_trees,
        breakpoint::skip_breakpoints,
        breakpoint::skip_all_breakpoints,
        breakpoint::terminate_debugging,
        source::request_source_file
    ]
}
