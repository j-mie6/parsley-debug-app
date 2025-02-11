use std::{collections::HashMap, sync::Mutex};

#[cfg(test)]
use mockall::automock;
use tauri::{Emitter, Manager};

use crate::trees::{DebugNode, DebugTree};


/* Global app state managed by Tauri */
pub struct AppState {
    tree: Option<DebugTree>, /* Parser tree that we may have */
    map: HashMap<u32, DebugNode>, /* Map from node_id to the respective node */
}

impl AppState {
    /* Create new AppState with no parser */
    pub fn new() -> Self {
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

    /* Get parser tree */
    pub fn get_tree(&self) -> Option<&DebugTree> {
        self.tree.as_ref()
    }

    fn setup_map(&mut self, debug_node: &DebugNode) {
        self.map.insert(debug_node.node_id, debug_node.clone());
        debug_node.children.iter().for_each(|child| self.setup_map(child));
    }

    pub fn get_debug_node(&self, node_id: u32) -> Option<&DebugNode> {
        self.map.get(&node_id)
    }

}



#[cfg_attr(test, automock)]
pub trait StateManager: Send + Sync + 'static {
    fn set_tree(&self, tree: DebugTree);

    fn get_tree(&self) -> DebugTree;

    fn get_debug_node(&self, node_id: u32) -> DebugNode;
}



pub struct StateHandle(Box<dyn StateManager>);

impl StateHandle {
    pub fn new<S: StateManager>(state: S) -> Self {
        StateHandle(Box::new(state))
    }
}

impl StateManager for StateHandle {
    fn set_tree(&self, tree: DebugTree) {
        self.0.as_ref().set_tree(tree);
    }

    fn get_tree(&self) -> DebugTree {
        self.0.as_ref().get_tree()
    }

    fn get_debug_node(&self, node_id: u32) -> DebugNode {
        self.0.as_ref().get_debug_node(node_id)
    }
}


impl StateManager for tauri::AppHandle {
    fn set_tree(&self, tree: DebugTree) {
        self.state::<Mutex<AppState>>()
            .lock()
            .expect("Failed to acquire lock")
            .set_tree(tree);

        /* EMIT TO FRONTEND FROM HERE */
        self.emit("tree-ready", ())
            .expect("Could not find ready tree");
    }

    fn get_tree(&self) -> DebugTree {
        self.state::<Mutex<AppState>>()
            .lock()
            .expect("Failed to acquire lock")
            .tree
            .as_ref()
            .expect("Tree has not been loaded")
            .clone()
    }

    fn get_debug_node(&self, node_id: u32) -> DebugNode {
        self.state::<Mutex<AppState>>()
            .lock()
            .expect("Failed to acquire lock")
            .get_debug_node(node_id)
            .expect(format!("Expected a debug node for {}", node_id).as_str())
            .clone()
    }
}
