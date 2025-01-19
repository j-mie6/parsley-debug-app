use crate::DebugTree;

pub type ParsleyDebugTree = String; //TODO: represent tree correctly

impl Into<DebugTree> for ParsleyDebugTree {
    fn into(self) -> DebugTree {
        DebugTree { } // TODO: convert to DebugTree correctly
    }
}


#[derive(serde::Deserialize)]
pub struct Data {
    pub input: String,
    pub tree: ParsleyDebugTree,
}


#[cfg(test)]
mod test {
    /* Data unit testing */
    
}