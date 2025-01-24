use crate::DebugNode;

/* Represents tree received from parsley-debug-views' Remote View*/
#[derive(serde::Deserialize, Clone)]
pub struct ParsleyNode {
    pub name: String, /* The user-defined name */
    pub internal: String, /* The internal name of the parser */
    pub success: bool, /* Whether the parser was successful */
    pub number: usize, /* The unique child number of this node */
    pub input: String, /* The input string passed to the parser */
    pub children: Vec<ParsleyNode>, /* The children of this node */
}

impl Into<DebugNode> for ParsleyNode {
    fn into(self) -> DebugNode {
        DebugNode { 
            name: self.name,
            internal: self.internal,
            success: self.success,
            input: self.input,
            number: self.number,
            children: self.children
                .into_iter()
                .map(ParsleyNode::into)
                .collect(),
        } 
    }
}


#[derive(serde::Deserialize)]
pub struct ParsleyTree {
    pub input: String,
    pub root: ParsleyNode,
}


#[cfg(test)]
pub mod test {
    /* Data unit testing */

    use crate::DebugNode;
    use super::{ParsleyTree, ParsleyNode};

    pub const RAW_TREE_SIMPLE: &str = r#"{
            "input": "Test",
            "root": {
                "name": "Test",
                "internal": "Test",
                "success": true,
                "number": 0,
                "input": "Test",
                "children": []
            }
        }"#;


    #[test]
    fn parsley_tree_deserialises() {
        let tree: ParsleyTree = serde_json::from_str(&RAW_TREE_SIMPLE).expect("Could not deserialise ParsleyTree");
        
        assert_eq!(tree.input, "Test");
        assert_eq!(tree.root.name, "Test");
        assert_eq!(tree.root.internal, "Test");
        assert_eq!(tree.root.success, true);
        assert_eq!(tree.root.number, 0);
        assert_eq!(tree.root.input, "Test");
        assert_eq!(tree.root.children.len(), 0);
    }

    #[test]
    fn nested_parsley_tree_deserialises() {
        let raw_tree: &str = r#"{
            "input": "Test",
            "root": {
                "name": "Test",
                "internal": "Test",
                "success": true,
                "number": 0,
                "input": "Test",
                "children": [
                    {
                        "name": "Test1",
                        "internal": "Test1",
                        "success": true,
                        "number": 0,
                        "input": "Test1",
                        "children": [
                            {
                                "name": "Test1.1",
                                "internal": "Test1.1",
                                "success": true,
                                "number": 0,
                                "input": "Test1.1",
                                "children": []
                            }
                        ]
                    },
                    {
                        "name": "Test2",
                        "internal": "Test2",
                        "success": true,
                        "number": 0,
                        "input": "Test2",
                        "children": [
                            {
                                "name": "Test2.1",
                                "internal": "Test2.1",
                                "success": true,
                                "number": 0,
                                "input": "Test2.1",
                                "children": []
                            }
                        ]
                    }
                ]
            }
        }"#;

        let tree: ParsleyTree = serde_json::from_str(&raw_tree).expect("Could not deserialise nested ParsleyTree");
        
        /* Check that the root tree has been serialised correctly */
        assert_eq!(tree.input, "Test");
        assert_eq!(tree.root.name, "Test");
        assert_eq!(tree.root.internal, "Test");
        assert_eq!(tree.root.success, true);
        assert_eq!(tree.root.number, 0);
        assert_eq!(tree.root.input, "Test");
        assert_eq!(tree.root.children.len(), 2);

        /* Check that the children have been serialised correctly */
        for (index, child) in tree.root.children.iter().enumerate() {
            assert_eq!(child.name, format!("Test{}", index + 1));
            assert_eq!(child.internal, format!("Test{}", index + 1));
            assert_eq!(child.success, true);
            assert_eq!(child.number, 0);
        }
    }

    
    #[test]
    fn parsley_debug_tree_converts_into_debug_tree() {
        let parsley_debug_tree: ParsleyNode = ParsleyNode {
            name: String::from("Test"),
            internal: String::from("Test"),
            success: true,
            number: 0,
            input: String::from("Test"),
            children: Vec::new()
        };
        
        let debug_tree: DebugNode = parsley_debug_tree.clone().into();
        
        assert_eq!(debug_tree.name, parsley_debug_tree.name);
        assert_eq!(debug_tree.internal, parsley_debug_tree.internal);
        assert_eq!(debug_tree.success, parsley_debug_tree.success);
        assert_eq!(debug_tree.number, parsley_debug_tree.number);
        assert_eq!(debug_tree.input, parsley_debug_tree.input);
        assert_eq!(debug_tree.children.len(), parsley_debug_tree.children.len());
    }

    #[test]
    fn nested_parsley_debug_tree_converts_into_debug_tree() {
        /* Root tree to test */
        let parsley_debug_tree: ParsleyNode = ParsleyNode {
            name: String::from("Test"),
            internal: String::from("Test"),
            success: true,
            number: 0,
            input: String::from("Test"),
            children: vec![
                ParsleyNode {
                    name: String::from("Test1"),
                    internal: String::from("Test1"),
                    success: true,
                    number: 0,
                    input: String::from("Test1"),
                    children: vec![
                        ParsleyNode {
                            name: String::from("Test1.1"),
                            internal: String::from("Test1.1"),
                            success: true,
                            number: 0,
                            input: String::from("Test1.1"),
                            children: Vec::new()
                        }
                    ]
                },
                ParsleyNode {
                    name: String::from("Test2"),
                    internal: String::from("Test2"),
                    success: true,
                    number: 0,
                    input: String::from("Test2"),
                    children: vec![
                        ParsleyNode {
                            name: String::from("Test2.1"),
                            internal: String::from("Test2.1"),
                            success: true,
                            number: 0,
                            input: String::from("Test2.1"),
                            children: Vec::new()
                        }
                    ]
                }
            ]
        };
        
        let debug_tree: DebugNode = parsley_debug_tree.clone().into();

        /* Check ParsleyNode to Node conversion */
        assert_eq!(debug_tree.name, parsley_debug_tree.name);
        assert_eq!(debug_tree.internal, parsley_debug_tree.internal);
        assert_eq!(debug_tree.success, parsley_debug_tree.success);
        assert_eq!(debug_tree.number, parsley_debug_tree.number);
        assert_eq!(debug_tree.input, parsley_debug_tree.input);
        assert_eq!(debug_tree.children.len(), parsley_debug_tree.children.len());

        /* Check nested children */
        for (index, child) in debug_tree.children.iter().enumerate() {
            assert_eq!(child.name, parsley_debug_tree.children[index].name);
            assert_eq!(child.internal, parsley_debug_tree.children[index].internal);
            assert_eq!(child.success, parsley_debug_tree.children[index].success);
            assert_eq!(child.number, parsley_debug_tree.children[index].number);
            assert_eq!(child.input, parsley_debug_tree.children[index].input);
            assert_eq!(child.children.len(), parsley_debug_tree.children[index].children.len());
        } 
    }
}