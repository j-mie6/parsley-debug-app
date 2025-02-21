package controller.tauri

enum Command(val name: String):
    /* Fetch commands */
    case FetchDebugTree extends Command("fetch_debug_tree")
    case FetchNodeChildren extends Command("fetch_node_children")

    /* Save commands */
    case SaveTree extends Command("save_tree")
    case FetchSavedTreeNames extends Command("fetch_saved_tree_names")
    case LoadSavedTree extends Command("load_saved_tree")
    case DeleteTree extends Command("delete_tree")

    /* Breakpoint commands */
    case SkipBreakpoints extends Command("skip_breakpoints")


object Command {
    given commandToString: Conversion[Command, String] with
        def apply(command: Command): String = command.name
}