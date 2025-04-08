use std::collections::HashMap;

use super::{DebugNode, DebugTree};

/* Struct identical to DebugTree that allows serialized saving */
#[derive(Debug, PartialEq, serde::Serialize, serde::Deserialize)]
pub struct SavedTree {
    input: String,
    root: SavedNode,
    parser_info: HashMap<String, Vec<(i32, i32)>>,
    is_debuggable: bool,
    refs: Vec<(i32, String)>,
    session_id: i32,
    #[serde(default = "SavedTree::default_session_name")] session_name: String,
}
impl SavedTree {
    pub fn new(input: String, root: SavedNode, parser_info: HashMap<String, Vec<(i32, i32)>>, is_debuggable: bool, refs: Vec<(i32, String)>, session_id: i32, session_name: String) -> Self {
        SavedTree {
            input,
            root,
            parser_info,
            is_debuggable,
            refs,
            session_id,
            session_name,
        }
    }

    fn default_session_name() -> String { String::from("run") }
}

#[derive(Debug, Clone, PartialEq, serde::Serialize, serde::Deserialize)]
pub struct SavedNode {
    node_id: u32,               /* The user-defined name */
    name: String,               /* The internal name of the parser */
    internal: String,           /* Whether the parser was successful */
    success: bool,              /* The unique child number of this node */
    child_id: Option<u32>,      /* Offset into the input in which this node's parse attempt starts */
    input: String,              /* Offset into the input in which this node's parse attempt finished */
    children: Vec<SavedNode>,   /* The children of this node */
    is_iterative: bool,         /* Whether this node needs bubbling (iterative and transparent) */
    newly_generated: bool,      /* Whether this node was generated since the previous breakpoint */
}
impl SavedNode {
    pub fn new(node_id: u32, name: String, internal: String, success: bool,
            child_id: Option<u32>, input: String, children: Vec<SavedNode>,
            is_iterative: bool, newly_generated: bool) -> Self {

        SavedNode {
            node_id,
            name,
            internal,
            success,
            child_id,
            input,
            children,
            is_iterative,
            newly_generated,
        }
    }
}

impl From<DebugTree> for SavedTree {
    fn from(debug_tree: DebugTree) -> Self {

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
                node.is_iterative,
                node.newly_generated,
            )
        }

        let node: SavedNode = convert_node(debug_tree.get_root().clone());

        SavedTree::new(debug_tree.get_input().clone(), node, debug_tree.get_parser_info().clone(), debug_tree.is_debuggable(), debug_tree.refs(), debug_tree.get_session_id(), String::from("tree"))
    }
}

impl From<SavedTree> for DebugTree {
    fn from(saved_tree: SavedTree) -> Self {
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
                node.is_iterative,
                node.newly_generated
            )
        }

        let node: DebugNode = convert_node(saved_tree.root.clone());
        DebugTree::new(saved_tree.input.clone(), node, saved_tree.parser_info.clone(), saved_tree.is_debuggable, saved_tree.refs, saved_tree.session_id/*, saved_tree.session_name.clone()*/)
    }
}

/* Saved Tree testing */
#[cfg(test)]
pub mod test {

    /* Saved Tree unit testing */

    use std::collections::HashMap;
    use std::io::Write;
    use std::fs::{self, File};

    use super::{SavedTree, SavedNode};
    use crate::trees::{debug_tree, DebugTree};

    pub fn json() -> String {
        r#"{
            "input": "Test",
            "root": {
                "node_id": 0,
                "name": "Test",
                "internal": "Test",
                "success": true,
                "child_id": 0,
                "input": "Test",
                "children": [],
                "is_iterative": false,
                "newly_generated": false
            },
            "parser_info" : {},
            "is_debuggable": false,
            "refs": [],
            "session_id": -1
        }"#
        .split_whitespace()
        .collect()
    }

    pub fn nested_json() -> String {
        r#"{
            "input": "01234",
            "root": {
                "node_id": 0,
                "name": "0",
                "internal": "0",
                "success": true,
                "child_id": 0,
                "input": "0",
                "children": [
                    {
                        "node_id": 1,
                        "name": "1",
                        "internal": "1",
                        "success": true,
                        "child_id": 1,
                        "input": "1",
                        "children": [
                            {
                                "node_id": 2,
                                "name": "2",
                                "internal": "2",
                                "success": true,
                                "child_id": 2,
                                "input": "2",
                                "children": [],
                                "is_iterative": false,
                                "newly_generated": false
                            }
                        ],
                        "is_iterative": false,
                        "newly_generated": false
                    },
                    {
                        "node_id": 3,
                        "name": "3",
                        "internal": "3",
                        "success": true,
                        "child_id": 3,
                        "input": "3",
                        "children": [
                            {
                                "node_id": 4,
                                "name": "4",
                                "internal": "4",
                                "success": true,
                                "child_id": 4,
                                "input": "4",
                                "children": [],
                                "is_iterative": false,
                                "newly_generated": false
                            }
                        ],
                        "is_iterative": false,
                        "newly_generated": false
                    }
                ],
                "is_iterative": false,
                "newly_generated": false
            },
            "parser_info" : {},
            "is_debuggable": false,
            "refs": [],
            "session_id": -1
        }"#
        .split_whitespace()
        .collect()
    }

    pub fn tree() -> SavedTree {
        SavedTree::new(
            String::from("Test"),
            SavedNode::new(
                0,
                String::from("Test"),
                String::from("Test"),
                true,
                Some(0),
                String::from("Test"),
                Vec::new(),
                false,
                false
            ),
            HashMap::new(),
            false,
            Vec::new(),
            -1,
            String::from("tree"),
        )
    }

    pub fn nested_tree() -> SavedTree {
        SavedTree::new(
            String::from("01234"),
            SavedNode::new(
                0,
                String::from("0"),
                String::from("0"),
                true,
                Some(0),
                String::from("0"),
                vec![
                    SavedNode::new(
                        1,
                        String::from("1"),
                        String::from("1"),
                        true,
                        Some(1),
                        String::from("1"),
                        vec![
                            SavedNode::new(
                                2,
                                String::from("2"),
                                String::from("2"),
                                true,
                                Some(2),
                                String::from("2"),
                                Vec::new(),
                                false,
                                false
                            )
                        ],
                        false,
                        false
                    ),
                    SavedNode::new(
                        3,
                        String::from("3"),
                        String::from("3"),
                        true,
                        Some(3),
                        String::from("3"),
                        vec![
                            SavedNode::new(
                                4,
                                String::from("4"),
                                String::from("4"),
                                true,
                                Some(4),
                                String::from("4"),
                                Vec::new(),
                                false,
                                false
                            )
                        ],
                        false,
                        false
                    )
                ],
                false,
                false
            ),
            HashMap::new(),
            false,
            Vec::new(),
            -1,
            String::from("tree"),
        )
    }


    #[test]
    fn saved_tree_serializes() {
        let json: String = serde_json::to_string(&tree())
            .expect("Tree should be able to serialize");

        assert_eq!(json, self::json());
    }

    #[test]
    fn saved_tree_deserializes() {
        let tree: SavedTree = serde_json::from_str(&json())
            .expect("Could not deserialise SavedTree");

        assert_eq!(tree, self::tree());
    }

    #[test]
    fn nested_saved_tree_serializes() {
        let json: String = serde_json::to_string(&nested_tree())
            .expect("Tree should be able to serialize");

        assert_eq!(json, nested_json());
    }

    #[test]
    fn nested_saved_tree_deserializes() {
        let tree: SavedTree = serde_json::from_str(&nested_json())
            .expect("Could not deserialise SavedTree");

        assert_eq!(tree, nested_tree());
    }


    #[test]
    fn saved_tree_converts_into_debug_tree() {
        let saved_tree: SavedTree = tree();
        let debug_tree: DebugTree = debug_tree::test::tree();

        assert_eq!(debug_tree, saved_tree.into());
    }

    #[test]
    fn debug_tree_converts_into_saved_tree() {
        let saved_tree: SavedTree = tree();
        let debug_tree: DebugTree = debug_tree::test::tree();

        assert_eq!(saved_tree, debug_tree.into());
    }

    #[test]
    fn nested_saved_tree_converts_into_nested_debug_tree() {
        let saved_tree: SavedTree = nested_tree();
        let debug_tree: DebugTree = debug_tree::test::nested_tree();

        assert_eq!(debug_tree, saved_tree.into());
    }

    #[test]
    fn nested_debug_tree_converts_into_nested_saved_tree() {
        let saved_tree: SavedTree = nested_tree();
        let debug_tree: DebugTree = debug_tree::test::nested_tree();

        assert_eq!(saved_tree, debug_tree.into());
    }


    #[test]
    fn saved_tree_saved_to_json() {
        const FILE_PATH: &str = "test_save.json";

        let mut data_file: File = File::create(FILE_PATH)
            .expect("File creation failed");

        write!(data_file, "{}", json())
            .expect("JSON could not be written to file");

        let contents: String = fs::read_to_string(FILE_PATH)
            .expect("File contents could not be read");

        assert_eq!(contents, json());

        fs::remove_file(FILE_PATH)
            .expect("File could not be deleted");
    }

    #[test]
    fn saved_tree_loaded_from_json() {
        const FILE_PATH: &str = "test_load.json";

        let mut data_file: File = File::create(FILE_PATH)
            .expect("File creation failed");

        write!(data_file, "{}", json())
            .expect("JSON could not be written to file");

        let contents: String = fs::read_to_string(FILE_PATH)
            .expect("File contents could not be read");

        let saved_tree: SavedTree = serde_json::from_str(&contents)
            .expect("Saved Tree could not be deserialised");

        assert_eq!(saved_tree, tree());

        fs::remove_file(FILE_PATH)
            .expect("File could not be deleted");
    }

}
