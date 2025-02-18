use super::{DebugNode, DebugTree};

/* Struct identical to DebugTree that allows serialized saving */
#[derive(Debug, PartialEq, serde::Serialize, serde::Deserialize)]
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
    pub fn new(
        node_id: u32, 
        name: String, 
        internal: String, 
        success: bool, 
        child_id: Option<u32>, 
        input: String, 
        children: Vec<SavedNode>
    ) -> Self {
        SavedNode {
            node_id,
            name,
            internal,
            success,
            child_id,
            input,
            children,
        }
    }
}



/* Saved Tree testing */
#[cfg(test)]
pub mod test {

    use std::{fs::{self, File}, io::Write, path};

    use crate::trees::{debug_tree, DebugTree};

    use super::{SavedTree, SavedNode};

    const TEST_TREE_DIR: &str = "./test_trees/";

    const TREE_JSON: &str = r#"{
  "input": "Test",
  "root": {
    "node_id": 0,
    "name": "Test",
    "internal": "Test",
    "success": true,
    "child_id": 0,
    "input": "Test",
    "children": []
  }
}"#;


    pub fn test_tree() -> SavedTree {
        SavedTree::new(
            String::from("Test"),
            SavedNode::new(
                0u32,
                String::from("Test"),
                String::from("Test"),
                true,
                Some(0),
                String::from("Test"),
                Vec::new(),
            ),
        )
    }


    #[test]
    fn converts_from_debug_to_saved() {
        let d_tree: DebugTree = debug_tree::test::test_tree();
        let s_tree: SavedTree = test_tree();

        assert_eq!(SavedTree::from(d_tree), s_tree);
    }

    #[test]
    fn converts_from_saved_to_debug() {
        let d_tree: DebugTree = debug_tree::test::test_tree();
        let s_tree: SavedTree = test_tree();

        assert_eq!(DebugTree::from(s_tree), d_tree);
    }

    #[test]
    fn saved_tree_serializes() {
        let s_tree: SavedTree = test_tree();
        let tree_str: String = serde_json::to_string_pretty(&s_tree).expect("Tree should be able to serialize");

        assert_eq!(tree_str, String::from(TREE_JSON));
    }


    #[test]
    fn saved_tree_deserializes() {
        let s_tree: SavedTree = test_tree();
        let saved_tree: SavedTree =
            serde_json::from_str(TREE_JSON).expect("Could not deserialise SavedTree");

        assert_eq!(s_tree, saved_tree);
    }

    fn create_dir() {
        if !path::Path::new(TEST_TREE_DIR).exists() {
            /* Will fail if dir is already there */
            let _res: std::io::Result<()> = fs::create_dir(TEST_TREE_DIR);
        }
    }

    #[test]
    fn can_save_and_retrieve_json_from_file() {
        create_dir();
        let tree_name: &str = "Test_json";


        let file_path: String = format!("{}{}.json", TEST_TREE_DIR, tree_name);
        let mut data_file: File = File::create(&file_path).expect("File should have been made");
    
        data_file.write(TREE_JSON.as_bytes()).expect("File should have been written to");

        let contents: String = fs::read_to_string(&file_path).expect("File contents should have been read");
        assert_eq!(contents, String::from(TREE_JSON));


        /* Clear test file */
        fs::remove_file(file_path).expect("File should have been deleted");
    }

    #[test]
    fn can_save_and_retrieve_tree_from_file() {
        create_dir();

        let tree_name: &str = "Test_tree";
        let tree_str: String = serde_json::to_string_pretty(&test_tree()).expect("Tree should be able to serialize");


        let file_path: String = format!("{}{}.json", TEST_TREE_DIR, tree_name);
        let mut data_file: File = File::create(&file_path).expect("File should have been made");
    
        data_file.write(tree_str.as_bytes()).expect("File should have been written to");



        let contents: String = fs::read_to_string(&file_path).expect("File contents should have been read");

        let saved_tree: SavedTree =
        serde_json::from_str(&contents).expect("Could not deserialise SavedTree");

        assert_eq!(test_tree(), saved_tree);

        /* Clear test file */
        fs::remove_file(file_path).expect("File should have been deleted");
    }
}
