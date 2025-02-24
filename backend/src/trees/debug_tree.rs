use super::{SavedTree, SavedNode};

/* Placeholder ParserInfo structures for state management */
#[derive(Clone, Debug, PartialEq, serde::Serialize)]
#[serde(rename_all = "camelCase")]
pub struct DebugTree {
    input: String,
    root: DebugNode,
    is_debuggable: bool,
}

impl DebugTree {
    pub fn new(input: String, root: DebugNode, is_debuggable: bool) -> Self {
        DebugTree { input, root, is_debuggable }
    }

    pub fn get_root(&self) -> &DebugNode {
        &self.root
    }

    pub fn get_input(&self) -> &String {
        &self.input
    }
}

impl From<SavedTree> for DebugTree {
    fn from(debug_tree: SavedTree) -> Self {
        /* Recursively convert children into SavedNodes */
        fn convert_node(node: SavedNode) -> DebugNode {
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
        DebugTree::new(debug_tree.get_input().clone(), node, false) // TODO?
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
    pub fn new(node_id: u32, name: String, internal: String, success: bool, 
            child_id: Option<u32>, input: String, children: Vec<DebugNode>) -> Self {

        DebugNode {
            node_id,
            name,
            internal,
            success,
            child_id,
            input,
            is_leaf: children.is_empty(),
            children,
        }
    }
}


#[cfg(test)]
pub mod test {

    /* Debug Tree unit testing */

    use super::{DebugNode, DebugTree};

    pub fn json() -> String {
        r#"{
            "input": "Test",
            "root": {
                "nodeId": 0,
                "name": "Test",
                "internal": "Test",
                "success": true,
                "childId": 0,
                "input": "Test",
                "isLeaf": true
            },
            "isDebuggable": false
        }"#
        .split_whitespace()
        .collect::<String>()
    }

    pub fn nested_json() -> String {
        r#"{
            "input": "01234",
            "root": {
                "nodeId": 0,
                "name": "0",
                "internal": "0",
                "success": true,
                "childId": 0,
                "input": "0",
                "isLeaf": false
            },
            "isDebuggable": false
        }"#
        .split_whitespace()
        .collect()
    }

    pub fn tree() -> DebugTree {
        DebugTree::new(
            String::from("Test"),
            DebugNode::new(
                0,
                String::from("Test"),
                String::from("Test"),
                true,
                Some(0),
                String::from("Test"),
                Vec::new()
            ),
            false
        )
    }

    pub fn nested_tree() -> DebugTree {
        DebugTree::new(
            String::from("01234"),
            DebugNode::new(
                0,
                String::from("0"),
                String::from("0"),
                true,
                Some(0),
                String::from("0"),
                vec![
                    DebugNode::new(
                        1,
                        String::from("1"),
                        String::from("1"),
                        true,
                        Some(1),
                        String::from("1"),
                        vec![
                            DebugNode::new(
                                2,
                                String::from("2"),
                                String::from("2"),
                                true,
                                Some(2),
                                String::from("2"),
                                vec![],
                            )
                        ]
                    ),
                    DebugNode::new(
                        3,
                        String::from("3"),
                        String::from("3"),
                        true,
                        Some(3),
                        String::from("3"),
                        vec![
                            DebugNode::new(
                                4,
                                String::from("4"),
                                String::from("4"),
                                true,
                                Some(4),
                                String::from("4"),
                                vec![],
                            )
                        ]
                    )
                ]
            ),
            false
        )
    }
 

    #[test]
    fn debug_tree_serialises() {
        let json: String = serde_json::to_string(&tree())
            .expect("Could not serialize DebugTree")
            .split_whitespace()
            .collect();

        assert_eq!(json, self::json());
    }

    #[test]
    fn nested_debug_tree_serialises() {
        let json: String = serde_json::to_string(&nested_tree())
            .expect("Could not serialize DebugTree")
            .split_whitespace()
            .collect();

        assert_eq!(json, nested_json());
    }

}

