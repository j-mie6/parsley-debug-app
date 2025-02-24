use crate::events::Event;

use crate::state::{StateError, StateManager};
use crate::trees::{DebugTree, DebugNode};

use super::TokioMutex;

pub type SkipsReceiver = rocket::tokio::sync::mpsc::Receiver<i32>;

/* Wrapper for StateManager implementation used for Rocket server state management */
pub struct ServerState(Box<dyn StateManager>, TokioMutex<SkipsReceiver>);

impl ServerState {
    pub fn new<S: StateManager>(state: S, rx: TokioMutex<SkipsReceiver>) -> Self {
        ServerState(Box::new(state), rx)
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

    fn transmit_breakpoint_skips(&self, skips: i32) -> Result<(),StateError> {
        self.0.as_ref().transmit_breakpoint_skips(skips)
    }
}

impl ServerState {
    pub async fn receive_breakpoint_skips(&self) -> Option<i32> {
        self.1.lock().await.recv().await
    }
}
