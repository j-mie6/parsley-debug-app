use std::collections::HashMap;

use crate::events::Event;
use crate::state::{StateError, StateManager};
use crate::trees::{DebugTree, DebugNode};

use super::TokioMutex;

pub type SkipsReceiver = rocket::tokio::sync::oneshot::Receiver<(i32, Vec<(i32, String)>)>;


/* Wrapper for StateManager implementation used for Rocket server state management */
pub struct ServerState(Box<dyn StateManager>, TokioMutex<HashMap<i32, SkipsReceiver>>);

impl ServerState {
    pub fn new<S: StateManager>(state: S) -> Self {
        ServerState(Box::new(state), TokioMutex::new(HashMap::new()))
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

    fn transmit_breakpoint_skips(&self, session_id: i32, skips: i32, new_refs: Vec<(i32, String)>) -> Result<(), StateError> {
        self.0.as_ref().transmit_breakpoint_skips(session_id, skips, new_refs)
    }
    
    fn add_session_id(&self, tree_name: String, session_id:i32) -> Result<(), StateError> {
        self.inner().add_session_id(tree_name, session_id)
    }
    
    fn rmv_session_id(&self, tree_name: String) -> Result<(), StateError> {
        self.inner().rmv_session_id(tree_name)
    }
    
    fn session_id_exists(&self, session_id:i32) -> Result<bool, StateError> {
        self.inner().session_id_exists(session_id)
    }
    
    fn get_session_ids(&self) -> Result<HashMap<String, i32>,StateError> {
        self.inner().get_session_ids()
    }

    fn next_session_id(&self) -> Result<i32,StateError> {
        self.inner().next_session_id()
    }
    
    fn new_transmitter(&self, session_id: i32, tx: rocket::tokio::sync::oneshot::Sender<(i32, Vec<(i32, String)>)>) -> Result<(), StateError> {
        self.inner().new_transmitter(session_id, tx)
    }
}

impl ServerState {
    pub fn new_receiver(&self, session_id: i32, rx: SkipsReceiver) -> Option<SkipsReceiver> {
        self.1.try_lock().ok().map(|mut map| map.insert(session_id, rx)).flatten()
    }

    pub async fn receive_breakpoint_skips(&self, session_id: i32) -> Option<(i32, Vec<(i32, String)>)> {
        let rx = self.1.lock().await.remove(&session_id);
        match rx {
            Some(rx) => rx.await.ok(),
            None => None,
        }
    }
}
