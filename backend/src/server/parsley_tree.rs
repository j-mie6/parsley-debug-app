use crate::{DebugNode, DebugTree};

/* Represents tree received from parsley-debug-views' Remote View*/
#[derive(Debug, Clone, PartialEq, serde::Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ParsleyNode {
    name: String,               /* The user-defined name */
    internal: String,           /* The internal name of the parser */
    success: bool,              /* Whether the parser was successful */
    child_id: i64,              /* The unique child number of this node */
    from_offset: i32,           /* Offset into the input in which this node's parse attempt starts */
    to_offset: i32,             /* Offset into the input in which this node's parse attempt finished */
    children: Vec<ParsleyNode>, /* The children of this node */
}

#[derive(Debug, PartialEq, serde::Deserialize)]
pub struct ParsleyTree {
    input: String,     /* The input string being parsed */
    root: ParsleyNode, /* Root node of the debug tree */
}

/* Convert from ParsleyTree to DebugTree */
impl From<ParsleyTree> for DebugTree {
    fn from(tree: ParsleyTree) -> Self {
        /* Helper function used to convert a ParsleyNode given the total input string */
        fn convert_node(node: ParsleyNode, input: &str, current_id: &mut u32) -> DebugNode {
            let node_id = *current_id; /* Holding the current node id to be passed on*/
            *current_id += 1; /* Incrementing the current node id for the next pass */

            /* Convert child_id, handling -1 case */
            let child_id: Option<u32> = node.child_id.try_into().ok();

            /* Slice the input into input consumed by this node, handling -1 case */
            let input_slice: String = usize::try_from(node.from_offset)
                .and_then(|from: usize| Ok(from..usize::try_from(node.to_offset)?))
                .map_or("", |range| &input[range])
                .to_string();

            /* Recursively convert children into DebugNodes */
            let children: Vec<DebugNode> = node
                .children
                .into_iter()
                .map(|child| convert_node(child, input, current_id))
                .collect();

            /* Instantiate DebugNode */
            DebugNode::new(
                node_id,
                node.name,
                node.internal,
                node.success,
                child_id,
                input_slice,
                children,
            )
        }

        /* BFS traversal for node_id */
        let mut current_id: u32 = 0;

        /* Convert the root node and return DebugTree */
        let node: DebugNode = convert_node(tree.root, &tree.input, &mut current_id);
        DebugTree::new(tree.input, node)
    }
}

#[cfg(test)]
pub mod test {
    /* Data unit testing */

    use super::{ParsleyNode, ParsleyTree};
    use crate::server::request::test::test_tree;
    use crate::{DebugNode, DebugTree};

    pub const RAW_TREE_SIMPLE: &str = r#"{
            "input": "Test",
            "root": {
                "name": "Test",
                "internal": "Test",
                "success": true,
                "childId": 0,
                "fromOffset": 0,
                "toOffset": 4,
                "children": []
            }
        }"#;

    fn test_parsley_tree() -> ParsleyTree {
        ParsleyTree {
            input: String::from("Test"),
            root: ParsleyNode {
                name: String::from("Test"),
                internal: String::from("Test"),
                success: true,
                child_id: 0,
                from_offset: 0,
                to_offset: 4,
                children: vec![],
            },
        }
    }

    #[test]
    fn parsley_tree_deserialises() {
        let parsley_tree: ParsleyTree =
            serde_json::from_str(&RAW_TREE_SIMPLE).expect("Could not deserialise ParsleyTree");

        assert_eq!(parsley_tree.input, "Test");
        assert_eq!(parsley_tree.root.name, "Test");
        assert_eq!(parsley_tree.root.internal, "Test");
        assert_eq!(parsley_tree.root.success, true);
        assert_eq!(parsley_tree.root.child_id, 0);
        assert_eq!(parsley_tree.root.from_offset, 0);
        assert_eq!(parsley_tree.root.to_offset, 4);
        assert_eq!(parsley_tree.root.children.len(), 0);
    }

    #[test]
    fn nested_parsley_tree_deserialises() {
        let raw_tree: &str = r#"{
            "input": "01234",
            "root": {
                "name": "0",
                "internal": "0",
                "success": true,
                "childId": 0,
                "fromOffset": 0,
                "toOffset": 1,
                "children": [
                    {
                        "name": "1",
                        "internal": "1",
                        "success": true,
                        "childId": 1,
                        "fromOffset": 1,
                        "toOffset": 2,
                        "children": [
                            {
                                "name": "2",
                                "internal": "2",
                                "success": true,
                                "childId": 2,
                                "fromOffset": 2,
                                "toOffset": 3,
                                "children": []
                            }
                        ]
                    },
                    {
                        "name": "3",
                        "internal": "3",
                        "success": true,
                        "childId": 3,
                        "fromOffset": 3,
                        "toOffset": 4,
                        "children": [
                            {
                                "name": "4",
                                "internal": "4",
                                "success": true,
                                "childId": 4,
                                "fromOffset": 4,
                                "toOffset": 5,
                                "children": []
                            }
                        ]
                    }
                ]
            }
        }"#;

        let parsley_tree: ParsleyTree =
            serde_json::from_str(&raw_tree).expect("Could not deserialise nested ParsleyTree");

        /* Check that the root tree has been serialised correctly */
        assert_eq!(parsley_tree.input, "01234");
        assert_eq!(parsley_tree.root.name, "0");
        assert_eq!(parsley_tree.root.internal, "0");
        assert_eq!(parsley_tree.root.success, true);
        assert_eq!(parsley_tree.root.child_id, 0);
        assert_eq!(parsley_tree.root.from_offset, 0);
        assert_eq!(parsley_tree.root.to_offset, 1);
        assert_eq!(parsley_tree.root.children.len(), 2);

        /* Check the first child */
        let child1 = &parsley_tree.root.children[0];
        assert_eq!(child1.name, "1");
        assert_eq!(child1.internal, "1");
        assert_eq!(child1.success, true);
        assert_eq!(child1.child_id, 1);
        assert_eq!(child1.from_offset, 1);
        assert_eq!(child1.to_offset, 2);
        assert_eq!(child1.children.len(), 1);

        /* Check the first grandchild of the first child */
        let child1_1 = &child1.children[0];
        assert_eq!(child1_1.name, "2");
        assert_eq!(child1_1.internal, "2");
        assert_eq!(child1_1.success, true);
        assert_eq!(child1_1.child_id, 2);
        assert_eq!(child1_1.from_offset, 2);
        assert_eq!(child1_1.to_offset, 3);
        assert_eq!(child1_1.children.len(), 0);

        /* Check the second child */
        let child2 = &parsley_tree.root.children[1];
        assert_eq!(child2.name, "3");
        assert_eq!(child2.internal, "3");
        assert_eq!(child2.success, true);
        assert_eq!(child2.child_id, 3);
        assert_eq!(child2.from_offset, 3);
        assert_eq!(child2.to_offset, 4);
        assert_eq!(child2.children.len(), 1);

        /* Check the first grandchild of the second child */
        let child2_1 = &child2.children[0];
        assert_eq!(child2_1.name, "4");
        assert_eq!(child2_1.internal, "4");
        assert_eq!(child2_1.success, true);
        assert_eq!(child2_1.child_id, 4);
        assert_eq!(child2_1.from_offset, 4);
        assert_eq!(child2_1.to_offset, 5);
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
            root: ParsleyNode {
                name: String::from("0"),
                internal: String::from("0"),
                success: true,
                child_id: 0,
                from_offset: 0,
                to_offset: 1,
                children: vec![
                    ParsleyNode {
                        name: String::from("1"),
                        internal: String::from("1"),
                        success: true,
                        child_id: 1,
                        from_offset: 1,
                        to_offset: 2,
                        children: vec![ParsleyNode {
                            name: String::from("2"),
                            internal: String::from("2"),
                            success: true,
                            child_id: 2,
                            from_offset: 2,
                            to_offset: 3,
                            children: Vec::new(),
                        }],
                    },
                    ParsleyNode {
                        name: String::from("3"),
                        internal: String::from("3"),
                        success: true,
                        child_id: 3,
                        from_offset: 3,
                        to_offset: 4,
                        children: vec![ParsleyNode {
                            name: String::from("4"),
                            internal: String::from("4"),
                            success: true,
                            child_id: -1,
                            from_offset: 4,
                            to_offset: 5,
                            children: Vec::new(),
                        }],
                    },
                ],
            },
        };

        let debug_tree: DebugTree = DebugTree::new(
            String::from("01234"),
            DebugNode::new(
                0u32,
                String::from("0"),
                String::from("0"),
                true,
                Some(0),
                String::from("0"),
                vec![
                    DebugNode::new(
                        1u32,
                        String::from("1"),
                        String::from("1"),
                        true,
                        Some(1),
                        String::from("1"),
                        vec![DebugNode::new(
                            2u32,
                            String::from("2"),
                            String::from("2"),
                            true,
                            Some(2),
                            String::from("2"),
                            vec![],
                        )],
                    ),
                    DebugNode::new(
                        3u32,
                        String::from("3"),
                        String::from("3"),
                        true,
                        Some(3),
                        String::from("3"),
                        vec![DebugNode::new(
                            4u32,
                            String::from("4"),
                            String::from("4"),
                            true,
                            None,
                            String::from("4"),
                            vec![],
                        )],
                    ),
                ],
            ),
        );

        /* Check ParsleyNode to Node conversion */
        assert_eq!(debug_tree, parsley_debug_tree.into());
    }
}
