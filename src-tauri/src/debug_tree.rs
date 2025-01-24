/* Placeholder ParserInfo structures for state management */
#[derive(Clone, Debug, PartialEq, serde::Serialize)]
pub struct DebugTree {
    input: String,
    root: DebugNode,
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
    pub fn new(name: String, internal: String, success: bool, number: usize, input: String, children: Vec<DebugNode>) -> Self {
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