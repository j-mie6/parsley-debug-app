use tauri::Manager;
use std::sync::{Mutex, MutexGuard};
use rocket::{http, post, serde::json::Json};

use crate::{AppState, state::ParserInfo};
use super::data::Data;


/* Expose routes for mounting during launch */
pub fn routes() -> Vec<rocket::Route> {
    rocket::routes![get_hello, post_tree]
}


/* Placeholder GET request handler to print 'Hello world!' */
#[rocket::get("/")]
fn get_hello() -> String {
    String::from("Hello world!")
}


/* Post request handler to accept parser info */
#[post("/api/remote", format = "application/json", data = "<data>")] 
fn post_tree(data: Json<Data>, state: &rocket::State<tauri::AppHandle>) -> http::Status {
    /* Deserialise json data and convert to ParserInfo */
    let parser_info: ParserInfo = data.into_inner().into();
    
    /* Acquire the app_state via the state and mutex */
    let tauri_state: tauri::State<Mutex<AppState>> = state.state::<Mutex<AppState>>();
    let mut app_state: MutexGuard<AppState> = tauri_state.lock().expect("AppState mutex could not be acquired");

    /* Update the parser info */
    app_state.parser = Some(parser_info);

    /* Return OK status */
    http::Status::Ok
}



#[cfg(test)]
mod test {
    
    use crate::server::test::tracked_client;
    use rocket::{http, local::blocking};
    
    /* Request unit testing */
    
    #[test]
    fn get_responds_hello_world() {
        /* Launch rocket client via a blocking, tracked Client for debugging */
        let client: blocking::Client = tracked_client();
        
        /* Perform GET request to index route '/' */
        let response: blocking::LocalResponse = client.get(rocket::uri!(super::get_hello)).dispatch();
        
        /* Assert GET request was successful and payload was correct */
        assert_eq!(response.status(), http::Status::Ok);
        assert_eq!(response.into_string().expect("Payload was not string"), "Hello world!");
    }
    
    #[test]
    fn unrouted_get_fails() {
        /* Launch rocket client via a blocking, tracked Client for debugging */
        let client: blocking::Client = tracked_client();
        
        /* Perform GET request to non-existent route '/hello' */
        let response: blocking::LocalResponse = client.get("/hello").dispatch();
        
        /* Assert GET request was unsuccessful with status 404 */
        assert_eq!(response.status(), http::Status::NotFound);
    }
    
    #[test]
    fn get_on_post_fails() {
        let client: blocking::Client = tracked_client();
        
        /* Perform GET request to '/api/remote' */
        let response: blocking::LocalResponse = client.get(rocket::uri!(super::post_tree)).dispatch();
        
        /* Assert that GET failed due to no found GET handlers */
        assert_eq!(response.status(), http::Status::NotFound);
    }
    
    
    #[test]
    fn post_tree_succeeds() {
        let client: blocking::Client = tracked_client();
        
        /* Perform POST request to '/api/remote' */
        let response: blocking::LocalResponse = client.post(rocket::uri!(super::post_tree))
            .header(http::ContentType::JSON)
            .body(r#"{"input": "this is the parser input", "tree": "tree"}"#)
            .dispatch();
        
        /* Assert that POST succeeded */
        assert_eq!(response.status(), http::Status::Ok);
    }
    
    #[test]
    fn empty_post_fails() {
        let client: blocking::Client = tracked_client();
        
        /* Perform POST request to '/api/remote' */
        let response: blocking::LocalResponse = client.post(rocket::uri!(super::post_tree))
            .header(http::ContentType::JSON)
            .body("{}")
            .dispatch();
        
        /* Assert that POST failed */
        assert_eq!(response.status(), http::Status::UnprocessableEntity);
    }
    
    #[test]
    fn bad_format_post_fails() {
        let client: blocking::Client = tracked_client();
        
        /* Perform POST request to '/api/remote' */
        let response: blocking::LocalResponse = client.post(rocket::uri!(super::post_tree))
            .header(http::ContentType::Text) /* Incompatible header type */
            .body("Hello world")
            .dispatch();
        
        /* Assert that POST failed */
        assert_eq!(response.status(), http::Status::NotFound);
    }
    
}