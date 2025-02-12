use crate::state::{StateError, StateManager};
use crate::trees::{DebugTree, DebugNode};


/* Wrapper for StateManager implementation used for Rocket server state management */
pub struct ServerState(Box<dyn StateManager>);

impl ServerState {
    pub fn new<S: StateManager>(state: S) -> Self {
        ServerState(Box::new(state))
    }
}

/* Delegate StateManager implementations to wrapped StateManager */
impl StateManager for ServerState {
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
