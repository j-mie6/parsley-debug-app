use super::{StateError, StateManager};
use crate::trees::{DebugTree, DebugNode};


pub struct StateHandle(Box<dyn StateManager>);

impl StateHandle {
    pub fn new<S: StateManager>(state: S) -> Self {
        StateHandle(Box::new(state))
    }
}

impl StateManager for StateHandle {
    fn set_tree(&self, tree: DebugTree) -> Result<(), StateError> {
        self.0.as_ref().set_tree(tree)
    }

    fn get_tree(&self) -> Result<DebugTree, StateError> {
        self.0.as_ref().get_tree()
    }

    fn get_node(&self, id: u32) -> Result<DebugNode, StateError> {
        self.0.as_ref().get_node(id)
    }
}
