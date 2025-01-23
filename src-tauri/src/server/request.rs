use std::{ops::Deref, sync::{Mutex, MutexGuard}};
use rocket::{get, http, post, serde::json::Json};

use crate::ParserInfo;
use super::payload::Payload;


/* Expose routes for mounting during launch */
pub fn routes() -> Vec<rocket::Route> {
    rocket::routes![get_index, get_info, post_payload]
}


/* Placeholder GET request handler to print 'Hello world!' */
#[get("/")]
fn get_index() -> String {
    String::from("DILL: Debugging Interactively for the ParsLey Language")
}


/* Post request handler to accept parser info */
#[post("/api/remote", format = "application/json", data = "<data>")] 
fn post_payload(data: Json<Payload>, state: &rocket::State<Mutex<ParserInfo>>) -> http::Status {
    /* Deserialise and unwrap json data */
    let Payload { input, tree } = data.into_inner(); 
    
    /* Acquire the mutex */
    let mut state = state.lock().expect("ParserInfo mutex could not be acquired");
    state.set_input(input);
    state.set_tree(tree.into());

    http::Status::Ok
}

#[get("/api/remote")]
fn get_info(state: &rocket::State<Mutex<ParserInfo>>) -> String {
    let state: MutexGuard<ParserInfo> = state.inner().lock().expect("ParserInfo mutex could not be acquired");
    serde_json::to_string_pretty(state.deref()).expect("Could not serialise ParserInfo to JSON")
}

#[cfg(test)]
pub mod test {
    
    use crate::server::test::tracked_client;
    use crate::server::payload::{ParsleyDebugTree, Payload};
    use rocket::{http, local::blocking};


    pub fn test_payload_str() -> String {
        serde_json::to_string_pretty(
            &Payload {
                input: String::from("Test"),
                tree: ParsleyDebugTree {
                    name: String::from("Test"),
                    internal: String::from("Test"),
                    success: true,
                    number: 0,
                    input: String::from("Test"),
                    children: vec![]
                },
            }
        ).expect("Could not serialise Payload to JSON")
    }
    
    /* Request unit testing */
    
    #[test]
    fn get_responds_onboarding() {
        /* Launch rocket client via a blocking, tracked Client for debugging */
        let client: blocking::Client = tracked_client();
        
        /* Perform GET request to index route '/' */
        let response: blocking::LocalResponse = client.get(rocket::uri!(super::get_index)).dispatch();
        
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
    fn post_payload_succeeds() {
        let client: blocking::Client = tracked_client();
        
        /* Perform POST request to '/api/remote' */
        let response: blocking::LocalResponse = client.post(rocket::uri!(super::post_payload))
            .header(http::ContentType::JSON)
            .body(&test_payload_str())
            .dispatch();
        
        /* Assert that POST succeeded */
        assert_eq!(response.status(), http::Status::Ok);
    }
    
    #[test]
    fn empty_post_fails() {
        let client: blocking::Client = tracked_client();
        
        /* Perform POST request to '/api/remote' */
        let response: blocking::LocalResponse = client.post(rocket::uri!(super::post_payload))
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
        let response: blocking::LocalResponse = client.post(rocket::uri!(super::post_payload))
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
        let response: blocking::LocalResponse = client.get(rocket::uri!(super::get_info)).dispatch();
        
        /* Assert that GET succeeded */
        assert_eq!(response.status(), http::Status::Ok);
    }

    #[test]
    fn get_returns_posted_tree() {
        let client: blocking::Client = tracked_client();

       /* Perform POST request to '/api/remote' */
       let post_response: blocking::LocalResponse = client.post(rocket::uri!(super::post_payload))
            .header(http::ContentType::JSON)
            .body(&test_payload_str())
            .dispatch();
   
        /* Assert that POST succeeded */
        assert_eq!(post_response.status(), http::Status::Ok); 

        /* Perform GET request to '/api/remote' */
        let get_response: blocking::LocalResponse = client.get(rocket::uri!(super::get_info)).dispatch();

        /* Assert that GET succeeded */
        assert_eq!(get_response.status(), http::Status::Ok);

       /* Assert that we GET the expected tree */
       assert_eq!(get_response.into_string().expect("Tree was not string"),
            test_payload_str()); 
    }
    
}