use std::collections::HashMap;

use crate::trees::{DebugTree, DebugNode};


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

