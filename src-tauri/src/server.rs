use std::sync::Mutex;

mod post;

/* Placeholder ParserInfo structures for state management */
#[allow(dead_code)] // Satisfy clippy - TODO: remove
pub struct ParserInfo {
	input: String,
	tree: DebugTree,
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


/* Mount the Rocket server to the running instance of Tauri */
pub fn mount() {
    /* Placeholder parser info struct */
    let parser_info: ParserInfo = ParserInfo {
        input: String::from("This is a parser input"),
        tree: DebugTree { }
    };

    /* Spawn the Rocket server */
    tauri::async_runtime::spawn(async move {
        rocket::build()
            .mount("/", rocket::routes![hello, post::post]) /* Mount routes to the base path '/' */
            .manage(Mutex::new(parser_info)) /* Manage the parser info as a mutex-protected state */
            .launch() /* Launch the Rocket server */
            .await
            .expect("Rocket failed to initialise");
    });
}

/* Placeholder GET request handler to print 'Hello world!' */
#[rocket::get("/")]
fn hello() -> String {
    String::from("Hello world!")
}



#[cfg(test)]
mod test {

    // Server integration testing
    
}