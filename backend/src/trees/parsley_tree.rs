use std::collections::HashMap;

use super::{DebugNode, DebugTree};

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

    /* Whether this node needs bubbling (iterative and transparent) */
    #[serde(default = "ParsleyTree::default_bool")] is_iterative: bool,

    /* Whether this node was generated since the previous breakpoint */
    #[serde(default = "ParsleyTree::default_bool")] newly_generated: bool,
}

#[derive(Debug, PartialEq, serde::Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ParsleyTree {
    input: String,              /* The input string being parsed */
    root: ParsleyNode,          /* Root node of the debug tree */

    parser_info: HashMap<String, Vec<(i32, i32)>>, /* Map from srcFile => List of parser indexes */

    /* If this tree was produced by a currently-running parser */
    #[serde(default = "ParsleyTree::default_bool")] is_debuggable: bool,

    /* State references to be modified */
    #[serde(default = "Vec::new")] refs: Vec<(i32, String)>,

    /* If this tree was produced by a currently-running parser */
    #[serde(default = "ParsleyTree::default_session_id")] session_id: i32,

    /* The name for this session, should it be provided */
    #[serde(default)] session_name: Option<String>,
}

impl ParsleyTree {
    /* Function used by serde to parse default boolean values as false */
    fn default_bool() -> bool { false }

    pub fn session_not_set(&self) -> bool { self.session_id == Self::default_session_id() }

    const fn default_session_id() -> i32 { -1 }
    fn default_session_name() -> String { String::from("tree") }

    pub fn set_session_id(&mut self, session_id: i32) {
        self.session_id = session_id
    }
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
                node.is_iterative,
                node.newly_generated,
            )
        }

        /* BFS traversal for node_id */
        let mut current_id: u32 = 0;

        /* Convert the root node and return DebugTree */
        let node: DebugNode = convert_node(tree.root, &tree.input, &mut current_id);
        let session_name = tree.session_name.unwrap_or(ParsleyTree::default_session_name());
        DebugTree::new(tree.input, node, tree.parser_info, tree.is_debuggable, tree.refs, tree.session_id, session_name)
    }
}


#[cfg(test)]
pub mod test {

    /* Data unit testing */

    use std::collections::HashMap;

    use super::{ParsleyNode, ParsleyTree};
    use crate::trees::{debug_tree, DebugTree};

    const DEFAULT_SESSION_ID: i32 = ParsleyTree::default_session_id();
    const DEFAULT_SESSION_NAME: &str = "tree";

    pub fn json() -> String {
        r#"{
            "input": "Test",
            "root": {
                "name": "Test",
                "internal": "Test",
                "success": true,
                "childId": 0,
                "fromOffset": 0,
                "toOffset": 4,
                "children": [],
                "isIterative": false
            },
            "parserInfo" : {},
            "isDebuggable": false,
            "refs": []
        }"#
        .split_whitespace()
        .collect()
    }

    pub fn nested_json() -> String {
        r#"{
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
                                "children": [],
                                "isIterative": false
                            }
                        ],
                        "isIterative": false
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
                                "children": [],
                                "isIterative": false
                            }
                        ],
                        "isIterative": false
                    }
                ],
                "isIterative": false
            },
            "parserInfo" : {},
            "isDebuggable": false,
            "refs": []
        }"#
        .split_whitespace()
        .collect()
    }

    pub fn tree() -> ParsleyTree {
        ParsleyTree {
            input: String::from("Test"),
            root: ParsleyNode {
                name: String::from("Test"),
                internal: String::from("Test"),
                success: true,
                child_id: 0,
                from_offset: 0,
                to_offset: 4,
                children: Vec::new(),
                is_iterative: false,
                newly_generated: false,
            },
            parser_info: HashMap::new(),
            is_debuggable: false,
            refs: Vec::new(),
            session_id: DEFAULT_SESSION_ID,
            session_name: Some(String::from(DEFAULT_SESSION_NAME)),
        }
    }

    pub fn nested_tree() -> ParsleyTree {
        ParsleyTree {
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
                        children: vec![
                            ParsleyNode {
                                name: String::from("2"),
                                internal: String::from("2"),
                                success: true,
                                child_id: 2,
                                from_offset: 2,
                                to_offset: 3,
                                children: Vec::new(),
                                is_iterative: false,
                                newly_generated: false,
                            }
                        ],
                        is_iterative: false,
                        newly_generated: false,
                    },
                    ParsleyNode {
                        name: String::from("3"),
                        internal: String::from("3"),
                        success: true,
                        child_id: 3,
                        from_offset: 3,
                        to_offset: 4,
                        children: vec![
                            ParsleyNode {
                                name: String::from("4"),
                                internal: String::from("4"),
                                success: true,
                                child_id: 4,
                                from_offset: 4,
                                to_offset: 5,
                                children: Vec::new(),
                                is_iterative: false,
                                newly_generated: false,
                            }
                        ],
                        is_iterative: false,
                        newly_generated: false,
                    }
                ],
                is_iterative: false,
                newly_generated: false,
            },
            parser_info: HashMap::new(),
            is_debuggable: false,
            refs: Vec::new(),
            session_id: DEFAULT_SESSION_ID,
            session_name: Some(String::from(DEFAULT_SESSION_NAME)),
        }
    }


    #[test]
    fn parsley_tree_deserialises() {
        let tree: ParsleyTree = serde_json::from_str(&json())
            .expect("Could not deserialise ParsleyTree");

        assert_eq!(tree, self::tree());
    }

    #[test]
    fn nested_parsley_tree_deserialises() {
        let tree: ParsleyTree = serde_json::from_str(&nested_json())
            .expect("Could not deserialise nested ParsleyTree");

        assert_eq!(tree, nested_tree());
    }

    #[test]
    fn parsley_debug_tree_converts_into_debug_tree() {
        let parsley_tree: ParsleyTree = tree();
        let debug_tree: DebugTree = debug_tree::test::tree();

        assert_eq!(debug_tree, parsley_tree.into());
    }

    #[test]
    fn nested_parsley_debug_tree_converts_into_debug_tree() {
        let parsley_tree: ParsleyTree = nested_tree();
        let debug_tree: DebugTree = debug_tree::test::nested_tree();

        assert_eq!(debug_tree, parsley_tree.into());
    }

}
