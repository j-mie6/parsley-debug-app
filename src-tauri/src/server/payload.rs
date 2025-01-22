use crate::DebugTree;

/* Represents tree received from parsley-debug-views' Remote View*/
#[allow(dead_code)] /* For fields that have been retained for compatibility
                    but are never read */
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
    
}