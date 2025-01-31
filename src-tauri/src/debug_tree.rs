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

    pub fn get_input(&self) -> &String {
        &self.input
    }
}


/* Defines tree structure used in backend that will be passed to frontend */
#[derive(Debug, Clone, PartialEq, serde::Serialize)]
#[serde(rename_all = "camelCase")]
pub struct DebugNode {
    pub name: String, /*The internal (default) or user-defined name of the parser */
    pub internal: String, /*The internal name of the parser */
    pub success: bool, /* Whether the parser was successful */
    pub child_id: Option<u32>, /* The unique child number of this node */
    pub input: String, /* The input string passed to the parser */
    pub children: Vec<DebugNode>, /* The children of this node */
}

impl DebugNode {
    pub fn new(name: String, internal: String, success: bool, child_id: Option<u32>, input: String, children: Vec<DebugNode>) -> Self {
        DebugNode {
            name,
            internal,
            success,
            child_id,
            input,
            children
        }
    }
}