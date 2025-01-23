
/* Placeholder ParserInfo structures for state management */
#[derive(serde::Serialize)]
pub struct ParserInfo {
    pub input: String,
    pub tree: DebugTree,
}

/* Defines tree structure used in backend that will be passed to frontend */
#[derive(serde::Serialize)]
pub struct DebugTree {
    pub name: String, /*The internal (default) or user-defined name of the parser */
    pub internal: String, /*The internal name of the parser */
    pub success: bool, /* Whether the parser was successful */
    pub number: usize, /* The unique child number of this node */
    pub input: String, /* The input string passed to the parser */
    pub children: Vec<DebugTree>, /* The children of this node */
}

impl DebugTree {
    pub fn new(name: String, internal: String, success: bool, number:usize, input: String, children: Vec<DebugTree>) -> Self {
        DebugTree {
            name,
            internal,
            success,
            number,
            input,
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

    pub fn set_input(&mut self, input: String) {
        self.input = input
    }
    
    pub fn set_tree(&mut self, tree: DebugTree) {
        self.tree = tree
    }
}
