use std::sync::{Mutex, MutexGuard};
use rocket::{get, http, post, serde::json::Json};

use crate::ParserInfo;
use super::payload::Payload;


/* Expose routes for mounting during launch */
pub fn routes() -> Vec<rocket::Route> {
    rocket::routes![get_onboarding, get_tree, post_tree]
}


/* Placeholder GET request handler to print 'Hello world!' */
#[get("/")]
fn get_onboarding() -> String {
    String::from("DILL: Debugging Interactively for the ParsLey Language")
}


/* Post request handler to accept parser info */
#[post("/api/remote", format = "application/json", data = "<data>")] 
fn post_tree(data: Json<Payload>, state: &rocket::State<Mutex<ParserInfo>>) -> http::Status {
    /* Deserialise and unwrap json data */
    let Payload { input, tree } = data.into_inner(); 
    
    /* Acquire the mutex */
    let mut state = state.lock().expect("ParserInfo mutex could not be acquired");
    state.set_input(input);
    state.set_tree(tree.into());

    http::Status::Ok
}

#[get("/api/remote")]
fn get_tree(state: &rocket::State<Mutex<ParserInfo>>) -> String {
    let state: MutexGuard<ParserInfo> = state.lock().expect("ParserInfo mutex could not be acquired");
    serde_json::to_string_pretty(&state.tree).expect("Could not serialise DebugTree to JSON")
}

#[cfg(test)]
mod test {
    
    use crate::server::test::tracked_client;
    use rocket::{http, local::blocking};
    
    /* Request unit testing */
    
    #[test]
    fn get_responds_onboarding() {
        /* Launch rocket client via a blocking, tracked Client for debugging */
        let client: blocking::Client = tracked_client();
        
        /* Perform GET request to index route '/' */
        let response: blocking::LocalResponse = client.get(rocket::uri!(super::get_onboarding)).dispatch();
        
        /* Assert GET request was successful and payload was correct */
        assert_eq!(response.status(), http::Status::Ok);
        assert_eq!(response.into_string().expect("Payload was not string"),
         "DILL: Debugging Interactively for the ParsLey Language");
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

    #[test]
    fn get_returns_tree() {
        let client: blocking::Client = tracked_client();
        
        /* Perform GET request to '/api/remote' */
        let response: blocking::LocalResponse = client.get(rocket::uri!(super::get_tree)).dispatch();
        
        /* Assert that GET succeeded */
        assert_eq!(response.status(), http::Status::Ok);
    }

    fn post_then_get_returns_tree() {
        let client: blocking::Client = tracked_client();

       /* Perform POST request to '/api/remote' */
       let post_response: blocking::LocalResponse = client.post(rocket::uri!(super::post_tree))
       .header(http::ContentType::JSON)
       .body(r#"{"input": "this is the test parser input", "tree": "test tree"}"#)
       .dispatch();
   
        /* Assert that POST succeeded */
        assert_eq!(post_response.status(), http::Status::Ok); 

        /* Perform GET request to '/api/remote' */
        let get_response: blocking::LocalResponse = client.get(rocket::uri!(super::get_tree)).dispatch();

        /* Assert that GET succeeded */
        assert_eq!(get_response.status(), http::Status::Ok);

       /* Assert that we GET the expected tree */
       assert_eq!(get_response.into_string().expect("Tree was not string"),
       r#"{"input": "this is the test parser input", "tree": "test tree"}"#); 
    }
    
}