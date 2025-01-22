
/* Placeholder ParserInfo structures for state management */
pub struct ParserInfo {
    pub input: String,
    pub tree: DebugTree,
}

/* Defines tree structure used in backend that will be passed to frontend */
#[derive(serde::Serialize)]
pub struct DebugTree {
    pub name: String, /*The internal (default) or user-defined name of the parser */
    pub success: bool, /* Whether the parser was successful */
    pub input: String, /* The input string passed to the parser */
    pub children: Vec<DebugTree>, /* The children of this node */
}

impl DebugTree {
    pub fn new(name: String, success: bool, input: String, children: Vec<DebugTree>) -> Self {
        DebugTree {
            name,
            success,
            input,
            children
        }
    }

    pub fn default() -> Self {
        DebugTree {
            name: String::from(""),
            success: false,
            input: String::from(""),
            children: Vec::new()
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

    pub fn set_input(&mut self, input: String) {
        self.input = input
    }
    
    pub fn set_tree(&mut self, tree: DebugTree) {
        self.tree = tree
    }
}
