use crate::state::{DebugTree, ParserInfo};

/* Parsley representation for Debug Tree */
pub type ParsleyDebugTree = String; //TODO: represent tree correctly

/* Convert from DebugTree to ParsleyDebugTree */
impl Into<DebugTree> for ParsleyDebugTree {
    fn into(self) -> DebugTree {
        DebugTree(self) //TODO: convert to DebugTree correctly
    }
}


/* Data posted to server */
#[derive(serde::Deserialize)]
/* Data posted to server */
pub struct Data {
    input: String,
    tree: ParsleyDebugTree,
}

/* Convert from posted data to ParserInfo */
impl Into<ParserInfo> for Data {
    fn into(self) -> ParserInfo {
        ParserInfo::new(self.input, self.tree.into())
    }
}

#[cfg(test)]
mod test {
    /* Data unit testing */
    
}