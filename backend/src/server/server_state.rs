use rocket::tokio::sync::Mutex;
use rocket::tokio::sync::mpsc;

use crate::state::{StateError, StateManager};
use crate::trees::{DebugTree, DebugNode};


/* Wrapper for StateManager implementation used for Rocket server state management */
pub struct ServerState(Box<dyn StateManager>, Mutex<mpsc::Receiver<i32>>);

impl ServerState {
    pub fn new<S: StateManager>(state: S, rx: Mutex<mpsc::Receiver<i32>>) -> Self {
        ServerState(Box::new(state), rx)
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

    fn transmit_breakpoint_skips(&self, skips: i32) -> Result<(),StateError> {
        self.0.as_ref().transmit_breakpoint_skips(skips)
    }
}

impl ServerState {
    pub async fn receive_breakpoint_skips(&self) -> Result<i32, ()> {
        self.1.lock().await.recv().await.ok_or(())
    }
}