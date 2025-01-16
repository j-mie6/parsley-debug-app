
/* Placeholder ParserInfo structures for state management */
pub struct ParserInfo {
	input: String,
	tree: DebugTree,
}

impl ParserInfo {
    pub fn new(input: String, tree: DebugTree) -> Self {
        ParserInfo { 
            input,
            tree
        }
    }
}

pub struct DebugTree {}

impl ParserInfo {
	pub fn set_input(&mut self, input: String) {
		self.input = input
	}
	
	pub fn set_tree(&mut self, tree: DebugTree) {
        self.tree = tree
	}
}
