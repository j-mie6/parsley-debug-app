use std::ops::Deref; 
use std::sync::{Mutex, MutexGuard};
use rocket::{get, http, post, serde::json::Json};

use crate::DebugTree;
use super::parsley_tree::ParsleyTree;


/* Expose routes for mounting during launch */
pub fn routes() -> Vec<rocket::Route> {
    rocket::routes![get_index, get_info, post_tree]
}


/* Placeholder GET request handler to print 'Hello world!' */
#[get("/")]
fn get_index() -> String {
    String::from("DILL: Debugging Interactively for the ParsLey Language")
}

/* Post request handler to accept parser info */
#[post("/api/remote", format = "application/json", data = "<data>")] 
fn post_tree(data: Json<ParsleyTree>, state: &rocket::State<Mutex<DebugTree>>) -> http::Status {
    /* Deserialise and unwrap json data */
    let parsley_tree: ParsleyTree = data.into_inner();
    let debug_tree: DebugTree = parsley_tree.into();    
    
    /* Acquire the mutex */
    let mut state: MutexGuard<DebugTree> = state.lock()
        .expect("State mutex could not be acquired");

    *state = debug_tree;

    http::Status::Ok
}

/* Return posted DebugTree as JSON string */
#[get("/api/remote")]
fn get_info(state: &rocket::State<Mutex<DebugTree>>) -> String {
    let state: MutexGuard<DebugTree> = state.inner().lock().expect("State mutex could not be acquired");
    serde_json::to_string_pretty(state.deref()).expect("Could not serialise State to JSON")
}



#[cfg(test)]
pub mod test {
    
    use rocket::{http, local::blocking};
    use crate::server::test::tracked_client;
    use crate::server::parsley_tree::test::RAW_TREE_SIMPLE;
        
    /* Request unit testing */
    
    #[test]
    fn get_responds_onboarding() {
        /* Launch rocket client via a blocking, tracked Client for debugging */
        let client: blocking::Client = tracked_client();
        
        /* Perform GET request to index route '/' */
        let response: blocking::LocalResponse = client.get(rocket::uri!(super::get_index)).dispatch();
        
        /* Assert GET request was successful and ParsleyTree was correct */
        assert_eq!(response.status(), http::Status::Ok);
        assert_eq!(response.into_string().expect("ParsleyTree was not string"),
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
        let response: blocking::LocalResponse = client.post(rocket::uri!(super::post_tree))
            .header(http::ContentType::JSON)
            .body(&RAW_TREE_SIMPLE)
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
        let response: blocking::LocalResponse = client.get(rocket::uri!(super::get_info)).dispatch();
        
        /* Assert that GET succeeded */
        assert_eq!(response.status(), http::Status::Ok);
    }

    #[test]
    fn get_returns_posted_tree() {
        let client: blocking::Client = tracked_client();

        /* Perform POST request to '/api/remote' */
        let post_response: blocking::LocalResponse = client.post(rocket::uri!(super::post_tree))
            .header(http::ContentType::JSON)
            .body(&RAW_TREE_SIMPLE)
            .dispatch();

        /* Assert that POST succeeded */
        assert_eq!(post_response.status(), http::Status::Ok);

        /* Perform GET request to '/api/remote' */
        let get_response: blocking::LocalResponse = client.get(rocket::uri!(super::get_info)).dispatch();

        /* Assert that GET succeeded */
        assert_eq!(get_response.status(), http::Status::Ok);

        /* Assert that we GET the expected tree */
        assert_eq!(
            get_response.into_string()
                .expect("get_info response is not a String")
                .replace(" ", ""),
                RAW_TREE_SIMPLE.replace(" ", "")
        );
    }
    
}