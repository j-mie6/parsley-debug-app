use crate::DebugTree;

#[derive(serde::Deserialize)] /* Support Json deserialisation */
pub struct ParsleyDebugTree { } //TODO: populate struct correctly


impl Into<DebugTree> for ParsleyDebugTree {
    fn into(self) -> DebugTree {
        DebugTree { } // TODO: convert DebugTree correctly
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