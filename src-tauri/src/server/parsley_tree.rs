use crate::{DebugNode, DebugTree};


/* Represents tree received from parsley-debug-views' Remote View*/
#[derive(Debug, Clone, PartialEq, serde::Deserialize)]
pub struct ParsleyNode {
    name: String, /* The user-defined name */
    internal: String, /* The internal name of the parser */
    success: bool, /* Whether the parser was successful */
    child_id: usize, /* The unique child number of this node */
    from_offset: usize, /* Offset into the input in which this node's parse attempt starts */
    to_offset: usize, /* Offset into the input in which this node's parse attempt finished */
    input: String, /* The input string passed to the parser */
    children: Vec<ParsleyNode>, /* The children of this node */
}

impl From<ParsleyNode> for DebugNode {
    fn from(node: ParsleyNode) -> Self {
        DebugNode::new( 
            node.name,
            node.internal,
            node.success,
            node.child_id,
            String::from(&node.input[node.from_offset..=node.to_offset]),
            node.children
                .into_iter()
                .map(ParsleyNode::into)
                .collect()
        )
    }
}


#[derive(Debug, PartialEq, serde::Deserialize)]
pub struct ParsleyTree {
    input: String,
    tree: ParsleyNode,
}

impl From<ParsleyTree> for DebugTree {
    fn from(tree: ParsleyTree) -> Self {
        DebugTree::new(tree.input, tree.tree.into())
    }
}



#[cfg(test)]
pub mod test {
    /* Data unit testing */

    use crate::{server::request::test::test_tree, DebugNode, DebugTree};
    use super::{ParsleyTree, ParsleyNode};

    pub const RAW_TREE_SIMPLE: &str = r#"{
            "input": "Test",
            "tree": {
                "name": "Test",
                "internal": "Test",
                "success": true,
                "child_id": 0,
                "from_offset": 0,
                "to_offset": 3,
                "input": "Test",
                "children": []
            }
        }"#;

    fn test_parsley_tree() -> ParsleyTree {
        ParsleyTree {
            input: String::from("Test"),
            tree: ParsleyNode {
                name: String::from("Test"),
                internal: String::from("Test"),
                success: true,
                child_id: 0,
                from_offset: 0,
                to_offset: 3,
                input: String::from("Test"),
                children: vec![]
            }
        }
    }


    #[test]
    fn parsley_tree_deserialises() {
        let parsley_tree: ParsleyTree = serde_json::from_str(&RAW_TREE_SIMPLE).expect("Could not deserialise ParsleyTree");
        
        assert_eq!(parsley_tree.input, "Test");
        assert_eq!(parsley_tree.tree.name, "Test");
        assert_eq!(parsley_tree.tree.internal, "Test");
        assert_eq!(parsley_tree.tree.success, true);
        assert_eq!(parsley_tree.tree.child_id, 0);
        assert_eq!(parsley_tree.tree.from_offset, 0);
        assert_eq!(parsley_tree.tree.to_offset, 3);
        assert_eq!(parsley_tree.tree.input, "Test");
        assert_eq!(parsley_tree.tree.children.len(), 0);
    }

    #[test]
    fn nested_parsley_tree_deserialises() {
        let raw_tree: &str = r#"{
            "input": "01234",
            "tree": {
                "name": "0",
                "internal": "0",
                "success": true,
                "child_id": 0,
                "from_offset": 0,
                "to_offset": 0,
                "input": "01234",
                "children": [
                    {
                        "name": "1",
                        "internal": "1",
                        "success": true,
                        "child_id": 1,
                        "from_offset": 1,
                        "to_offset": 1,
                        "input": "01234",
                        "children": [
                            {
                                "name": "2",
                                "internal": "2",
                                "success": true,
                                "child_id": 2,
                                "from_offset": 2,
                                "to_offset": 2,
                                "input": "01234",
                                "children": []
                            }
                        ]
                    },
                    {
                        "name": "3",
                        "internal": "3",
                        "success": true,
                        "child_id": 3,
                        "from_offset": 3,
                        "to_offset": 3,
                        "input": "01234",
                        "children": [
                            {
                                "name": "4",
                                "internal": "4",
                                "success": true,
                                "child_id": 4,
                                "from_offset": 4,
                                "to_offset": 4,
                                "input": "01234",
                                "children": []
                            }
                        ]
                    }
                ]
            }
        }"#;

        let parsley_tree: ParsleyTree = serde_json::from_str(&raw_tree).expect("Could not deserialise nested ParsleyTree");
        
        /* Check that the root tree has been serialised correctly */
        assert_eq!(parsley_tree.input, "01234");
        assert_eq!(parsley_tree.tree.name, "0");
        assert_eq!(parsley_tree.tree.internal, "0");
        assert_eq!(parsley_tree.tree.success, true);
        assert_eq!(parsley_tree.tree.child_id, 0);
        assert_eq!(parsley_tree.tree.from_offset, 0);
        assert_eq!(parsley_tree.tree.to_offset, 0);
        assert_eq!(parsley_tree.tree.input, "01234");
        assert_eq!(parsley_tree.tree.children.len(), 2);

        /* Check the first child */
        let child1 = &parsley_tree.tree.children[0];
        assert_eq!(child1.name, "1");
        assert_eq!(child1.internal, "1");
        assert_eq!(child1.success, true);
        assert_eq!(child1.child_id, 1);
        assert_eq!(child1.from_offset, 1);
        assert_eq!(child1.to_offset, 1);
        assert_eq!(child1.input, "01234");
        assert_eq!(child1.children.len(), 1);

        /* Check the first grandchild of the first child */
        let child1_1 = &child1.children[0];
        assert_eq!(child1_1.name, "2");
        assert_eq!(child1_1.internal, "2");
        assert_eq!(child1_1.success, true);
        assert_eq!(child1_1.child_id, 2);
        assert_eq!(child1_1.from_offset, 2);
        assert_eq!(child1_1.to_offset, 2);
        assert_eq!(child1_1.input, "01234");
        assert_eq!(child1_1.children.len(), 0);

        /* Check the second child */
        let child2 = &parsley_tree.tree.children[1];
        assert_eq!(child2.name, "3");
        assert_eq!(child2.internal, "3");
        assert_eq!(child2.success, true);
        assert_eq!(child2.child_id, 3);
        assert_eq!(child2.from_offset, 3);
        assert_eq!(child2.to_offset, 3);
        assert_eq!(child2.input, "01234");
        assert_eq!(child2.children.len(), 1);

        /* Check the first grandchild of the second child */
        let child2_1 = &child2.children[0];
        assert_eq!(child2_1.name, "4");
        assert_eq!(child2_1.internal, "4");
        assert_eq!(child2_1.success, true);
        assert_eq!(child2_1.child_id, 4);
        assert_eq!(child2_1.from_offset, 4);
        assert_eq!(child2_1.to_offset, 4);
        assert_eq!(child2_1.input, "01234");
        assert_eq!(child2_1.children.len(), 0);
    }

    
    #[test]
    fn parsley_debug_tree_converts_into_debug_tree() {
        let parsley_tree: ParsleyTree = test_parsley_tree();
        let debug_tree: DebugTree = test_tree();
        
        assert_eq!(debug_tree, parsley_tree.into());
    }

    #[test]
    fn nested_parsley_debug_tree_converts_into_debug_tree() {
        /* Root tree to test */
        let parsley_debug_tree: ParsleyTree = ParsleyTree {
            input: String::from("01234"),
            tree: ParsleyNode {
                name: String::from("0"),
                internal: String::from("0"),
                success: true,
                child_id: 0,
                from_offset: 0,
                to_offset: 0,
                input: String::from("01234"),
                children: vec![
                    ParsleyNode {
                        name: String::from("1"),
                        internal: String::from("1"),
                        success: true,
                        child_id: 1,
                        from_offset: 1,
                        to_offset: 1,
                        input: String::from("01234"),
                        children: vec![
                            ParsleyNode {
                                name: String::from("2"),
                                internal: String::from("2"),
                                success: true,
                                child_id: 2,
                                from_offset: 2,
                                to_offset: 2,
                                input: String::from("01234"),
                                children: Vec::new()
                            }
                        ]
                    },
                    ParsleyNode {
                        name: String::from("3"),
                        internal: String::from("3"),
                        success: true,
                        child_id: 3,
                        from_offset: 3,
                        to_offset: 3,
                        input: String::from("01234"),
                        children: vec![
                            ParsleyNode {
                                name: String::from("4"),
                                internal: String::from("4"),
                                success: true,
                                child_id: 4,
                                from_offset: 4,
                                to_offset: 4,
                                input: String::from("01234"),
                                children: Vec::new()
                            }
                        ]
                    }
                ]
            }
        };

        let debug_tree: DebugTree = DebugTree::new(
            String::from("01234"), 
            DebugNode::new(
                String::from("0"), 
                String::from("0"), 
                true, 
                0, 
                String::from("0"), 
                vec![
                    DebugNode::new(
                        String::from("1"), 
                        String::from("1"), 
                        true, 
                        1, 
                        String::from("1"), 
                        vec![
                            DebugNode::new(
                                String::from("2"), 
                                String::from("2"), 
                                true, 
                                2, 
                                String::from("2"), 
                                vec![]
                            )
                        ]
                    ),
                    DebugNode::new(
                        String::from("3"), 
                        String::from("3"), 
                        true, 
                        3, 
                        String::from("3"), 
                        vec![
                            DebugNode::new(
                                String::from("4"), 
                                String::from("4"), 
                                true, 
                                4, 
                                String::from("4"), 
                                vec![]
                            )
                        ]
                    )
                ]
            )
        );
        
        /* Check ParsleyNode to Node conversion */
        assert_eq!(debug_tree, parsley_debug_tree.into());
    }
}