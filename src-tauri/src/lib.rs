use state::StateHandle;
use tauri::Manager;
use std::sync::{Mutex, MutexGuard};
use std::collections::HashMap;


use std::fs::{File, read_dir};
use std::io::Write;

mod server;
mod state;
mod debug_tree;
mod saved_tree;

pub use debug_tree::{DebugTree, DebugNode};
pub use saved_tree::SavedTree;


/* Global app state managed by Tauri */
struct AppState {
    tree: Option<DebugTree>, /* Parser tree that we may have */
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
        debug_node.children.iter().for_each(|child| self.setup_map(child));
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
        server::launch(StateHandle::new(handle)).await
            .expect("Rocket failed to initialise")
    });
    
    
    Ok(())
}

/* Run the Tauri app */
pub fn run() {
    tauri::Builder::default()
        .setup(setup) /* Run app setup */
        .invoke_handler(tauri::generate_handler![fetch_debug_tree, fetch_node_children, save_debug_tree, get_saved_trees]) /* Expose render_debug_tree() to frontend */
        .run(tauri::generate_context!()) /* Start up the app */
        .expect("error while running tauri application");
}

/* Frontend-accessible debug render */
#[tauri::command]
fn fetch_debug_tree(state: tauri::State<Mutex<AppState>>) -> String {
    /* Acquire the state mutex to access the parser */
    let tree: &Option<DebugTree> = &state.lock().expect("State mutex could not be acquired").tree;
    
    /* If parser exists, render in JSON */
    match tree {
        Some(tree) => serde_json::to_string_pretty(tree)
            .expect("Debug tree could not be serialised"),
        None => String::from(""),
    }
}

/* Backend reactive fetch children */
#[tauri::command]
fn fetch_node_children(state: tauri::State<Mutex<AppState>>, node_id: u32) -> Result<String, FetchChildrenError> {
    /* Acquire the state mutex to access the corresponding debug node */
    let state_guard: MutexGuard<AppState> = state.lock().map_err(|_| FetchChildrenError::LockFailed)?;

    /* Find node with corresponding node id */
    let node: &DebugNode = state_guard
        .get_debug_node(node_id)
        .ok_or(FetchChildrenError::NodeNotFound(node_id))?;

    /* Serialise children */
    serde_json::to_string_pretty(&node.children)
        .map_err(|_| FetchChildrenError::SerdeError)
}

#[derive(Debug, serde::Serialize)]
enum FetchChildrenError {
    LockFailed,
    NodeNotFound(u32),
    SerdeError,
}

/* Saves current tree to saved_trees/tree_name.json */
#[tauri::command]
fn save_debug_tree(state: tauri::State<Mutex<AppState>>) -> () {
    /* Acquire the state mutex to access the parser */
    let tree: &Option<DebugTree> = &state.lock().expect("State mutex could not be acquired").tree;
    
    /* If parser exists, get the JSON equivalent */
    let tree_json = match tree {
        Some(tree) => {
            serde_json::to_string_pretty(&SavedTree::from(tree))
                .expect("Debug tree could not be serialised")
        },
            
        None => String::from(""),
    };

    let tree_name: String = String::from("test");

    let file_path = format!("saved_trees/{}.json", tree_name);
    /* Create the json file to store the tree */
    let mut data_file = File::create(file_path).expect("File creation failed");

    /* Write tree json to the json file */
    data_file.write(tree_json.as_bytes()).expect("File write failed");
}

#[tauri::command]
fn get_saved_trees() -> Result<String, FetchChildrenError>  {
    let dir: &str = "./saved_trees/";
    let ext: &str = ".json";

    let paths = read_dir(dir).unwrap();

    let mut names: Vec<String> = Vec::new();
    for path in paths {
        let file_name: String = path.unwrap().file_name().into_string().ok().expect("Error getting filename");
        let name: &str = file_name.strip_suffix(ext).expect("File extension should be json");
        names.push(String::from(name));
    }

    /* Serialise names */
    serde_json::to_string_pretty(&names)
        .map_err(|_| FetchChildrenError::SerdeError)
}
#[cfg(test)]
mod test {
    
    /* Tauri integration tests */
    
}
