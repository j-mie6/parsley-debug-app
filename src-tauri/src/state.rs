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


