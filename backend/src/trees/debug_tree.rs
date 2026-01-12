use std::collections::HashMap;

/* Placeholder ParserInfo structures for state management */
#[derive(Clone, Debug, PartialEq, serde::Serialize)]
#[serde(rename_all = "camelCase")]
pub struct DebugTree {
    input: String,
    root: DebugNode,
    parser_info: HashMap<String, Vec<(i32, i32)>>,
    is_debuggable: bool,
    refs: Vec<(i32, String)>,
    session_id: i32,
    session_name: String,
}

impl DebugTree {
    pub fn new(input: String, root: DebugNode, parser_info: HashMap<String, Vec<(i32, i32)>>, is_debuggable: bool, refs: Vec<(i32, String)>,  session_id: i32, session_name: String) -> Self {
        DebugTree { input, root, parser_info, is_debuggable, refs, session_id, session_name }
    }

    pub fn get_root(&self) -> &DebugNode {
        &self.root
    }

    pub fn get_input(&self) -> &String {
        &self.input
    }

    pub fn get_parser_info(&self) -> &HashMap<String, Vec<(i32, i32)>> {
        &self.parser_info
    }

    pub fn is_debuggable(&self) -> bool {
        self.is_debuggable
    }

    pub fn refs(&self) -> Vec<(i32, String)> {
        self.refs.clone()
    }

    pub fn get_session_id(&self) -> i32 {
        self.session_id
    }

    pub fn set_is_debugging(&mut self, is_debug: bool) {
        self.is_debuggable = is_debug
    }

    pub fn set_session_id(&mut self, session_id: i32) {
        self.session_id = session_id
    }

    pub fn get_session_name(&self) -> String {
        self.session_name.clone()
    }
}

/* Defines tree structure used in backend that will be passed to frontend */
#[derive(Debug, Clone, PartialEq, serde::Serialize)]
#[serde(rename_all = "camelCase")]
pub struct DebugNode {
    pub node_id: u32,          /* The unique number of this node */
    pub name: String,          /* The internal (default) or user-defined name of the parser */
    pub internal: String,      /* The internal name of the parser */
    pub success: bool,         /* Whether the parser was successful */
    pub child_id: Option<u32>, /* The unique child number of this node */
    pub input_start: u32,      /* Index of start of consumed input */
    pub input_end: u32,        /* Index of end of consumed input (exclusive) */
    #[serde(skip_serializing)] pub children: Vec<DebugNode>, /* The children of this node */
    pub is_leaf: bool,         /* Whether this node is a leaf node */
    pub is_iterative: bool,    /* Whether this node needs bubbling (iterative and transparent) */
    pub newly_generated: bool, /* Whether this node was generated since the previous breakpoint */
}

impl DebugNode {
    pub fn new(node_id: u32, name: String, internal: String, success: bool,
            child_id: Option<u32>, input_start: u32, input_end: u32, children: Vec<DebugNode>,
            is_iterative: bool, newly_generated: bool) -> Self {

        DebugNode {
            node_id,
            name,
            internal,
            success,
            child_id,
            input_start,
            input_end,
            is_leaf: children.is_empty(),
            children,
            is_iterative,
            newly_generated
        }
    }
}


#[cfg(test)]
pub mod test {

    /* Debug Tree unit testing */

    use std::collections::HashMap;

    use super::{DebugNode, DebugTree};

    const DEFAULT_SESSION_ID: i32 = -1;
    const DEFAULT_SESSION_NAME: &str = "tree";

    pub fn json() -> String {
        format!(r#"{{
            "input": "Test",
            "root": {{
                "nodeId": 0,
                "name": "Test",
                "internal": "Test",
                "success": true,
                "childId": 0,
                "inputStart": 0,
                "inputEnd": 4,
                "isLeaf": true,
                "isIterative": false,
                "newlyGenerated": false
            }},
            "parserInfo" : {{}},
            "isDebuggable": false,
            "refs": [],
            "sessionId": {session_id},
            "sessionName": "{session_name}"
        }}"#, session_id = DEFAULT_SESSION_ID, session_name = DEFAULT_SESSION_NAME)
        .split_whitespace()
        .collect::<String>()
    }

    pub fn nested_json() -> String {
        format!(r#"{{
            "input": "01234",
            "root": {{
                "nodeId": 0,
                "name": "0",
                "internal": "0",
                "success": true,
                "childId": 0,
                "inputStart": 0,
                "inputEnd": 1,
                "isLeaf": false,
                "isIterative": false,
                "newlyGenerated": false
            }},
            "parserInfo" : {{}},
            "isDebuggable": false,
            "refs": [],
            "sessionId": {session_id},
            "sessionName": "{session_name}"
        }}"#, session_id = DEFAULT_SESSION_ID, session_name = DEFAULT_SESSION_NAME)
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
                0, 4,
                Vec::new(),
                false,
                false
            ),
            HashMap::new(),
            false,
            Vec::new(),
            DEFAULT_SESSION_ID,
            String::from(DEFAULT_SESSION_NAME),
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
                0, 1,
                vec![
                    DebugNode::new(
                        1,
                        String::from("1"),
                        String::from("1"),
                        true,
                        Some(1),
                        1, 2, 
                        vec![
                            DebugNode::new(
                                2,
                                String::from("2"),
                                String::from("2"),
                                true,
                                Some(2),
                                2, 3,
                                Vec::new(),
                                false,
                                false
                            )
                        ],
                        false,
                        false
                    ),
                    DebugNode::new(
                        3,
                        String::from("3"),
                        String::from("3"),
                        true,
                        Some(3),
                        3, 4,
                        vec![
                            DebugNode::new(
                                4,
                                String::from("4"),
                                String::from("4"),
                                true,
                                Some(4),
                                4, 5,
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
            DEFAULT_SESSION_ID,
            String::from(DEFAULT_SESSION_NAME),
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
