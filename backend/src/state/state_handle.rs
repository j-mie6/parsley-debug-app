use super::StateManager;
use crate::trees::{DebugTree, DebugNode};


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
