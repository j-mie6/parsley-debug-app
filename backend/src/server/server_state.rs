use crate::events::Event;
use crate::state::{StateError, StateManager};
use crate::trees::{DebugTree, DebugNode};


/* Wrapper for StateManager implementation used for Rocket server state management */
pub struct ServerState(Box<dyn StateManager>);

impl ServerState {
    pub fn new<S: StateManager>(state: S) -> Self {
        ServerState(Box::new(state))
    }

    /* Get wrapped StateManager implementation */
    fn inner(&self) -> &dyn StateManager {
        self.0.as_ref()
    }
}

/* Delegate StateManager implementations to wrapped StateManager */
impl StateManager for ServerState {
    fn set_tree(&self, tree: DebugTree) -> Result<(), StateError> {
        self.inner().set_tree(tree)
    }

    fn get_tree(&self) -> Result<DebugTree, StateError> {
        self.inner().get_tree()
    }

    fn get_node(&self, id: u32) -> Result<DebugNode, StateError> {
        self.inner().get_node(id)
    }

    fn emit<'a>(&self, event: Event<'a>) -> Result<(), StateError> {
        self.inner().emit(event)
    }
}
