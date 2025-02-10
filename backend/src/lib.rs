use state::StateHandle;
use std::collections::HashMap;
use std::sync::{Mutex, MutexGuard};
use tauri::Manager;

mod debug_tree;
mod server;
mod state;

pub use debug_tree::{DebugNode, DebugTree};

/* Global app state managed by Tauri */
struct AppState {
    tree: Option<DebugTree>,      /* Parser tree that we may have */
    map: HashMap<u32, DebugNode>, /* Map from node_id to the respective node */
}

impl AppState {
    /* Create new AppState with no parser */
    fn new() -> Self {
        AppState {
            tree: None,
            map: HashMap::new(),
        }
    }

    /* Set the parser tree */
    pub fn set_tree(&mut self, tree: DebugTree) {
        self.map.clear();
        self.setup_map(tree.get_root());
        self.tree = Some(tree);
    }

    fn setup_map(&mut self, debug_node: &DebugNode) {
        self.map.insert(debug_node.node_id, debug_node.clone());
        debug_node
            .children
            .iter()
            .for_each(|child| self.setup_map(child));
    }

    pub fn get_debug_node(&self, node_id: u32) -> Option<&DebugNode> {
        self.map.get(&node_id)
    }
}

/* Setup Tauri app */
fn setup(app: &mut tauri::App) -> Result<(), Box<dyn std::error::Error>> {
    if cfg!(debug_assertions) {
        app.handle().plugin(
            tauri_plugin_log::Builder::default()
                .level(log::LevelFilter::Info)
                .build(),
        )?;
    }

    /* Manage the app state using Tauri */
    app.manage(Mutex::new(AppState::new()));

    /* Clone the app handle for use by Rocket state */
    let handle: tauri::AppHandle = app.handle().clone();

    /* Mount the Rocket server to the running instance of Tauri */
    tauri::async_runtime::spawn(async move {
        server::launch(StateHandle::new(handle))
            .await
            .expect("Rocket failed to initialise")
    });

    Ok(())
}

/* Run the Tauri app */
pub fn run() {
    /* Fix for NVidia graphics cards */
    std::env::set_var("WEBKIT_DISABLE_DMABUF_RENDERER", "1");

    tauri::Builder::default()
        .plugin(tauri_plugin_shell::init())
        .setup(setup) /* Run app setup */
        .invoke_handler(tauri::generate_handler![
            fetch_debug_tree,
            fetch_node_children
        ]) /* Expose render_debug_tree() to frontend */
        .run(tauri::generate_context!()) /* Start up the app */
        .expect("error while running tauri application");
}

/* Frontend-accessible debug render */
#[tauri::command]
fn fetch_debug_tree(state: tauri::State<Mutex<AppState>>) -> String {
    /* Acquire the state mutex to access the parser */
    let tree: &Option<DebugTree> = &state
        .lock()
        .expect("State mutex could not be acquired")
        .tree;

    /* If parser exists, render in JSON */
    match tree {
        Some(tree) => {
            serde_json::to_string_pretty(tree).expect("Debug tree could not be serialised")
        }
        None => String::from(""),
    }
}

/* Backend reactive fetch children */
#[tauri::command]
fn fetch_node_children(
    state: tauri::State<Mutex<AppState>>,
    node_id: u32,
) -> Result<String, FetchChildrenError> {
    /* Acquire the state mutex to access the corresponding debug node */
    let state_guard: MutexGuard<AppState> =
        state.lock().map_err(|_| FetchChildrenError::LockFailed)?;

    /* Find node with corresponding node id */
    let node: &DebugNode = state_guard
        .get_debug_node(node_id)
        .ok_or(FetchChildrenError::NodeNotFound(node_id))?;

    /* Serialise children */
    serde_json::to_string_pretty(&node.children).map_err(|_| FetchChildrenError::SerdeError)
}

#[derive(Debug, serde::Serialize)]
enum FetchChildrenError {
    LockFailed,
    NodeNotFound(u32),
    SerdeError,
}

#[cfg(test)]
mod test {

    /* Tauri integration tests */
}
