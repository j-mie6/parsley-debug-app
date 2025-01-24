#[cfg(test)] use mockall::automock;
use tauri::Manager;
use std::sync::Mutex;

use crate::{AppState, DebugTree};

pub struct StateHandle(Box<dyn StateManager>);

#[cfg_attr(test, automock)]
pub trait StateManager : Send + Sync + 'static {
    fn set_tree(&self, tree: DebugTree);

    fn get_tree(&self) -> DebugTree;
}

impl StateHandle {
    pub fn new<S : StateManager>(state: S) -> Self {
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
}

impl StateManager for tauri::AppHandle {
    fn set_tree(&self, tree: DebugTree) {
        self.state::<Mutex<AppState>>().lock().unwrap().tree = Some(tree)
    }
    
    fn get_tree(&self) -> DebugTree {
        self.state::<Mutex<AppState>>().lock().unwrap().tree.as_ref().unwrap().clone()
    }
}
