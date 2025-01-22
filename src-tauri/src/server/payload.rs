use crate::DebugTree;

/* Represents tree received from parsley-debug-views' Remote View*/
#[derive(serde::Deserialize)]
pub struct ParsleyDebugTree {
    pub name: String, /* The user-defined name of the tree */
    pub internal: String, /*The internal name of the parser */
    pub success: bool, /* Whether the parser was successful */
    pub number: usize, /* The unique child number of this node */
    pub input: String, /* The input string passed to the parser */
    pub children: Vec<ParsleyDebugTree>, /* The children of this node */
}

impl Into<DebugTree> for ParsleyDebugTree {
    fn into(self) -> DebugTree {
        DebugTree { 
            name: self.name,
            internal: self.internal,
            success: self.success,
            input: self.input,
            number: self.number,
            children: self.children
                .into_iter()
                .map(|child| child.into())
                .collect(),
        } 
    }
}


#[derive(serde::Deserialize)]
pub struct Payload {
    pub input: String,
    pub tree: ParsleyDebugTree,
}


#[cfg(test)]
mod test {
    /* Data unit testing */

    use crate::DebugTree;
    use super::ParsleyDebugTree;
    
    #[test]
    fn parsley_debug_tree_into_debug_tree() {
        
        
        let parsley_debug_tree: ParsleyDebugTree = ParsleyDebugTree {
            name: String::from("Test"),
            internal: String::from("Test"),
            success: true,
            number: 0,
            input: String::from("Test"),
            children: Vec::new()
        };
        
        let debug_tree: DebugTree = parsley_debug_tree.into();
        
        assert_eq!(debug_tree.name, parsley_debug_tree.name);
        assert_eq!(debug_tree.internal, parsley_debug_tree.internal);
        assert_eq!(debug_tree.success, parsley_debug_tree.success);
        assert_eq!(debug_tree.number, parsley_debug_tree.number);
        assert_eq!(debug_tree.input, parsley_debug_tree.input);
        assert_eq!(debug_tree.children.len(), parsley_debug_tree.children.len());
    }
}