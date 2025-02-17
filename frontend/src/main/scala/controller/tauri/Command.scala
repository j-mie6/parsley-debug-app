package controller.tauri

enum Command(val name: String):
    /* Fetch commands */
    case FetchDebugTree extends Command("fetch_debug_tree")
    case FetchNodeChildren extends Command("fetch_node_children")

    /* Save commands */
    case SaveTree extends Command("save_tree")
    case FetchSavedTreeNames extends Command("fetch_saved_tree_names")
    case LoadSavedTree extends Command("load_saved_tree")
