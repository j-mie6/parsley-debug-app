#[cfg(test)] use mockall::automock;
use tauri::Manager;
use std::sync::Mutex;

use crate::AppState;

/* Placeholder ParserInfo structures for state management */
#[derive(serde::Serialize, Debug, PartialEq)]
pub struct ParserInfo {
    pub input: String,
    pub tree: DebugTree,
}

/* Defines tree structure used in backend that will be passed to frontend */
#[derive(Debug, Clone, PartialEq, serde::Serialize)]
pub struct DebugTree {
    pub name: String, /*The internal (default) or user-defined name of the parser */
    pub internal: String, /*The internal name of the parser */
    pub success: bool, /* Whether the parser was successful */
    pub input: String, /* The input string passed to the parser */
    pub number: usize, /* The unique child number of this node */
    pub children: Vec<DebugTree>, /* The children of this node */
}

impl DebugTree {
    pub fn new(name: String, internal: String, success: bool, input: String, number:usize, children: Vec<DebugTree>) -> Self {
        DebugTree {
            name,
            internal,
            success,
            input,
            number,
            children
        }
    }
}

impl ParserInfo {
    pub fn new(input: String, tree: DebugTree) -> Self {
        ParserInfo { 
            input,
            tree
        }
    }
    
    pub fn get_tree(&self) -> &DebugTree {
        &self.tree
    }
}


pub struct StateHandle(Box<dyn StateManager>);
impl StateHandle {
    pub fn new<S : 'static + StateManager>(state: S) -> Self {
        StateHandle(Box::new(state))
    }
}

impl StateManager for StateHandle {
    fn set_info(&self, info: ParserInfo) {
        self.0.as_ref().set_info(info);
    }

    fn get_tree(&self) -> DebugTree {
        self.0.as_ref().get_tree()
    }
}


#[cfg_attr(test, automock)]
pub trait StateManager : Send + Sync {
    fn set_info(&self, info: ParserInfo);

    fn get_tree(&self) -> DebugTree;
}


impl StateManager for tauri::AppHandle {
    fn set_info(&self, info: ParserInfo) {
        self.state::<Mutex<AppState>>().lock().unwrap().parser = Some(info)
    }
    
    fn get_tree(&self) -> DebugTree {
        self.state::<Mutex<AppState>>().lock().unwrap().parser.as_ref().unwrap().tree.clone()
    }
}