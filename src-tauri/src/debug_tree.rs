/* Placeholder ParserInfo structures for state management */
#[derive(Clone, Debug, PartialEq, serde::Serialize)]
pub struct DebugTree {
    input: String,
    root: DebugNode,
}

impl DebugTree {
    pub fn new(input: String, root: DebugNode) -> Self {
        DebugTree { input, root }
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
    pub node_id: u32,          /* The unique number of this node */
    pub name: String,          /*The internal (default) or user-defined name of the parser */
    pub internal: String,      /*The internal name of the parser */
    pub success: bool,         /* Whether the parser was successful */
    pub child_id: Option<u32>, /* The unique child number of this node */
    pub input: String,         /* The input string passed to the parser */
    #[serde(skip_serializing)]
    pub children: Vec<DebugNode>, /* The children of this node */
    pub is_leaf: bool,
}

impl DebugNode {
    pub fn new(
        node_id: u32,
        name: String,
        internal: String,
        success: bool,
        child_id: Option<u32>,
        input: String,
        children: Vec<DebugNode>,
    ) -> Self {
        let is_leaf: bool = children.is_empty();
        DebugNode {
            node_id,
            name,
            internal,
            success,
            child_id,
            input,
            children,
            is_leaf,
        }
    }
}
