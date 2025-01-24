#[cfg(test)] use mockall::automock;
use tauri::Manager;
use std::sync::Mutex;

use crate::AppState;

/* Placeholder ParserInfo structures for state management */
#[derive(serde::Serialize, Clone, Debug, PartialEq)]
pub struct DebugTree {
    pub input: String,
    pub root: DebugNode,
}

impl DebugTree {
    pub fn new(input: String, root: DebugNode) -> Self {
        DebugTree { 
            input,
            root
        }
    }
    
    pub fn get_root(&self) -> &DebugNode {
        &self.root
    }
}


/* Defines tree structure used in backend that will be passed to frontend */
#[derive(Debug, Clone, PartialEq, serde::Serialize)]
pub struct DebugNode {
    pub name: String, /*The internal (default) or user-defined name of the parser */
    pub internal: String, /*The internal name of the parser */
    pub success: bool, /* Whether the parser was successful */
    pub input: String, /* The input string passed to the parser */
    pub number: usize, /* The unique child number of this node */
    pub children: Vec<DebugNode>, /* The children of this node */
}

impl DebugNode {
    pub fn new(name: String, internal: String, success: bool, input: String, number:usize, children: Vec<DebugNode>) -> Self {
        DebugNode {
            name,
            internal,
            success,
            input,
            number,
            children
        }
    }
}


pub struct StateHandle(Box<dyn StateManager>);

impl StateHandle {
    pub fn new<S : StateManager>(state: S) -> Self {
        StateHandle(Box::new(state))
    }
}

impl StateManager for StateHandle {
    fn set(&self, info: DebugTree) {
        self.0.as_ref().set(info);
    }

    fn get_tree(&self) -> DebugTree {
        self.0.as_ref().get_tree()
    }
}


#[cfg_attr(test, automock)]
pub trait StateManager : Send + Sync + 'static {
    fn set(&self, info: DebugTree);

    fn get_tree(&self) -> DebugTree;
}


impl StateManager for tauri::AppHandle {
    fn set(&self, info: DebugTree) {
        self.state::<Mutex<AppState>>().lock().unwrap().parser = Some(info)
    }
    
    fn get_tree(&self) -> DebugTree {
        self.state::<Mutex<AppState>>().lock().unwrap().parser.as_ref().unwrap().clone()
    }
}
