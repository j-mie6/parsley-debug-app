use std::collections::HashMap;
use std::sync::{Mutex, MutexGuard};

use rocket::tokio::{self, sync::mpsc};

use crate::trees::{DebugTree, DebugNode};
use super::{StateError, StateManager, AppHandle};

/* Unsynchronised AppState */
struct AppStateInternal {
    app: AppHandle,                 /* Handle to instance of Tauri app, used for events */
    tree: Option<DebugTree>,        /* Parser tree that is posted to Server */
    map: HashMap<u32, DebugNode>,   /* Map from node_id to the respective node */
    break_tx: tokio::sync::Mutex<mpsc::Sender<i32>>, /* TODO */
}


/* Synchronised global app state wrapping internal AppState */
pub struct AppState(Mutex<AppStateInternal>);

impl AppState {
    /* Create a new app state with the app_handle */
    pub fn new(app_handle: tauri::AppHandle, break_tx: tokio::sync::Mutex<mpsc::Sender<i32>>) -> AppState {
        AppState(
            Mutex::new(
                AppStateInternal {
                    app: AppHandle::new(app_handle),
                    tree: None,
                    map: HashMap::new(),
                    break_tx,
                }
            )
        )
    }

    /* Access wrapped inner AppStateInternal struct */
    fn inner(&self) -> Result<MutexGuard<AppStateInternal>, StateError> {
        self.0.lock()
            .map_err(|_| StateError::LockFailed)
    }
}


impl StateManager for AppState {
    /* Update StateManager's tree */
    fn set_tree(&self, tree: DebugTree) -> Result<(), StateError> {
        let mut state: MutexGuard<AppStateInternal> = self.inner()?;
        
        /* Reset map */
        state.map.clear();
        
        /* Recursively insert nodes into map of node ids to nodes */
        fn insert_node(state: &mut MutexGuard<AppStateInternal>, node: &DebugNode) -> Result<(), StateError> {
            state.map.insert(node.node_id, node.clone());

            for child in node.children.iter() {
                insert_node(state, child)?;
            }
            
            Ok(())
        }

        insert_node(&mut state, tree.get_root())?;

        /* Update tree */
        state.tree = Some(tree);
        
        /* Notify frontend listener */
        state.app.emit("tree-ready", ())
            .map_err(|_| StateError::EventEmitFailed)
    }
    
    /* Get StateManager's tree */
    fn get_tree(&self) -> Result<DebugTree, StateError> {
        self.inner()?
            .tree
            .as_ref()
            .ok_or(StateError::TreeNotFound)
            .cloned()
    }
    
    /* Get node associated with node ID */
    fn get_node(&self, id: u32) -> Result<DebugNode, StateError> {
        self.inner()?
            .map
            .get(&id)
            .ok_or(StateError::NodeNotFound(id))
            .cloned()
    }

    fn transmit_breakpoint_skips(&self, skips: i32) -> Result<(), StateError> {
        self.inner()?.break_tx.blocking_lock().blocking_send(skips);
        Ok(())
    }
}
