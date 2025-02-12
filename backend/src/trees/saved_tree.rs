use super::{DebugNode, DebugTree};

/* Struct identical to DebugTree that allows serialized saving */
#[derive(Debug, serde::Serialize, serde::Deserialize)]
pub struct SavedTree {
    input: String,
    root: SavedNode,
}

#[derive(Debug, Clone, PartialEq, serde::Serialize, serde::Deserialize)]
pub struct SavedNode {
    pub node_id: u32,
    pub name: String,
    pub internal: String,
    pub success: bool,
    pub child_id: Option<u32>,
    pub input: String,
    pub children: Vec<SavedNode>,
    pub is_leaf: bool,
}

impl From<&DebugTree> for SavedTree {
    fn from(debug_tree: &DebugTree) -> Self {

        fn convert_node(node: DebugNode) -> SavedNode {
            /* Recursively convert children into SavedNodes */
            let children: Vec<SavedNode> = node.children
                .into_iter()
                .map(convert_node)
                .collect();
    
            /* Instantiate SavedNode */
            SavedNode::new(
                node.node_id, 
                node.name,
                node.internal,
                node.success,
                node.child_id,
                node.input,
                children,
                node.is_leaf
            )
        }

        let node: SavedNode = convert_node(debug_tree.get_root().clone());
  
        SavedTree::new(debug_tree.get_input().clone(), node)
    }
}

impl SavedTree {
    pub fn new(input: String, root: SavedNode) -> Self {
        SavedTree { 
            input,
            root
        }
    }

    pub fn get_root(&self) -> &SavedNode {
        &self.root
    }

    pub fn get_input(&self) -> &String {
        &self.input
    }
}

impl SavedNode {
    pub fn new(node_id: u32, name: String, internal: String, success: bool, child_id: Option<u32>, input: String, children: Vec<SavedNode>, is_leaf: bool) -> Self {
        SavedNode {
            node_id,
            name,
            internal,
            success,
            child_id,
            input,
            children,
            is_leaf
        }
    }
}



#[cfg(test)]
pub mod test {
    /* Saved Tree unit testing */

    use super::SavedTree;

    pub const RAW_TREE: &str = ""; //TODO

    pub fn test_tree() -> SavedTree {
        todo!("SavedTree test example does not exist")
    }

}
