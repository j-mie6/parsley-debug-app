/* Placeholder ParserInfo structures for state management */
#[derive(serde::Serialize)]
pub struct ParserInfo {
    input: String,
    tree: DebugTree,
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


#[derive(serde::Serialize)]
pub struct DebugTree(pub String); 

