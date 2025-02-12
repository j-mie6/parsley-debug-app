use super::{SavedTree, SavedNode};

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

impl From<&SavedTree> for DebugTree {
    fn from(debug_tree: &SavedTree) -> Self {

      fn convert_node(node: SavedNode) -> DebugNode {
        /* Recursively convert children into SavedNodes */
        let children: Vec<DebugNode> = node.children
            .into_iter()
            .map(convert_node)
            .collect();

        /* Instantiate SavedNode */
        DebugNode::new(
            node.node_id, 
            node.name,
            node.internal,
            node.success,
            node.child_id,
            node.input,
            children,
        )
      }
      let node: DebugNode = convert_node(debug_tree.get_root().clone());

      DebugTree::new(debug_tree.get_input().clone(), node)
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
    #[serde(skip_serializing)] pub children: Vec<DebugNode>, /* The children of this node */
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




#[cfg(test)]
pub mod test {
    /* Debug Tree unit testing */

    use super::{DebugNode, DebugTree};


    pub const RAW_TREE: &str = r#"{
        "input": "Test",
        "root": {
            "nodeId": 0,
            "name": "Test",
            "internal": "Test",
            "success": true,
            "childId": 0,
            "input": "Test",
            "isLeaf": true
        }
    }"#;

    pub fn test_tree() -> DebugTree {
        DebugTree::new(
            String::from("Test"),
            DebugNode::new(
                0u32,
                String::from("Test"),
                String::from("Test"),
                true,
                Some(0),
                String::from("Test"),
                vec![],
            ),
        )
    }

}

