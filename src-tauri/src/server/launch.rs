use std::sync::Mutex;

use rocket::Rocket;

use crate::{DebugTree, ParserInfo};


/* Build the Rocket server */
pub fn build() -> Rocket<rocket::Build> {
    /* Placeholder parser info struct */
    let parser_info: ParserInfo = ParserInfo::new(
        String::from("This is a parser input"),
        DebugTree { }
    );
    
    rocket::build()
        .mount("/", super::request::routes()) /* Mount routes to the base path '/' */
        .manage(Mutex::new(parser_info)) /* Manage the parser info as a mutex-protected state */
}

/* Launch the Rocket server */
pub async fn launch() -> Rocket<rocket::Ignite> {
    build().launch().await
        .expect("Rocket failed to initialise")
}


#[cfg(test)]
mod test {
    
    /* Launch unit testing */

}