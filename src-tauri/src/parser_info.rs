
/* Placeholder DebugTree structures for state management */
pub struct DebugTree {
    pub input: String,
    pub root: Node,
}

/* Defines tree structure used in backend that will be passed to frontend */
#[derive(serde::Serialize)]
pub struct Node {
    pub name: String, /*The internal (default) or user-defined name of the parser */
    pub internal: String, /*The internal name of the parser */
    pub success: bool, /* Whether the parser was successful */
    pub input: String, /* The input string passed to the parser */
    pub number: usize, /* The unique child number of this node */
    pub children: Vec<Node>, /* The children of this node */
}

impl Node {
    pub fn new(name: String, internal: String, success: bool, input: String, number:usize, children: Vec<Node>) -> Self {
        Node {
            name,
            internal,
            success,
            input,
            number,
            children
        }
    }
}

impl DebugTree {
    pub fn new(input: String, root: Node) -> Self {
        DebugTree { 
            input,
            root
        }
    }

    pub fn set_input(&mut self, input: String) {
        self.input = input
    }
    
    pub fn set_tree(&mut self, root: Node) {
        self.root = root
    }
}
