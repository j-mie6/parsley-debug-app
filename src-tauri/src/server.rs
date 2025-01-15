use std::sync::Mutex;
use rocket::{Ignite, Rocket};

mod post;

/* Placeholder ParserInfo structures for state management */
#[allow(dead_code)] // Satisfy clippy - TODO: remove
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


/* Mount the Rocket server to the running instance of Tauri */
pub fn mount() {
    /* Spawn the Rocket server as a Tauri process */
    tauri::async_runtime::spawn(rocket());
}

/* Launch the Rocket server */
async fn rocket() -> Rocket<Ignite> {
    /* Placeholder parser info struct */
    let parser_info: ParserInfo = ParserInfo::new(
        String::from("This is a parser input"),
        DebugTree { }
    );

    rocket::build()
        .mount("/", rocket::routes![hello, post::post]) /* Mount routes to the base path '/' */
        .manage(Mutex::new(parser_info)) /* Manage the parser info as a mutex-protected state */
        .launch()
        .await
        .expect("Rocket failed to initialise")
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