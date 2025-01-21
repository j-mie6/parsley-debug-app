
/* Placeholder ParserInfo structures for state management */
pub struct ParserInfo {
    input: String,
    tree: DebugTree,
}

/* Defines tree structure used in backend that will be passed to frontend */
pub struct DebugTree {
    pub name: String, /*The internal (default) or user-defined name of the parser */
    pub success: bool, /* Whether the parser was successful */
    pub input: String, /* The input string passed to the parser */
    pub children: Vec<DebugTree>, /* The children of this node */
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
