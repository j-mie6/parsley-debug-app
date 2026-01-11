use std::path::PathBuf;
use std::collections::HashMap;

use crate::events::Event;
use crate::state::{StateError, StateManager};
use crate::trees::{DebugTree, DebugNode};
use crate::state::state_manager::{BreakpointCode, DirectoryKind, UpdateTreeError, ambassador_impl_StateManager};
use super::TokioMutex;

pub type SkipsSender = rocket::tokio::sync::oneshot::Sender<i32>;
type SkipsReceiver = rocket::tokio::sync::oneshot::Receiver<i32>;


/* Wrapper for StateManager implementation used for Rocket server state management */
pub struct ServerState(Box<dyn StateManager>, TokioMutex<HashMap<i32, SkipsReceiver>>);

/* Delegate StateManager implementations to wrapped StateManager */
#[ambassador::delegate_to_methods]
#[delegate(StateManager, target_ref = "inner")]
impl ServerState {
    pub fn new<S: StateManager>(state: S) -> Self {
        ServerState(Box::new(state), TokioMutex::new(HashMap::new()))
    }

    /* Get wrapped StateManager implementation */
    fn inner(&self) -> &dyn StateManager {
        self.0.as_ref()
    }
}

impl ServerState {
    pub fn new_receiver(&self, session_id: i32, rx: SkipsReceiver) -> Option<SkipsReceiver> {
        self.1.try_lock().ok().and_then(|mut map| map.insert(session_id, rx))
    }

    pub async fn receive_breakpoint_skips(&self, session_id: i32) -> Option<i32> {
        let rx = self.1.lock().await.remove(&session_id);
        match rx {
            Some(rx) => rx.await.ok(),
            None => None,
        }
    }
}
