use std::sync::Mutex;
use rocket::{post, serde::json::Json, http};

use crate::ParserInfo;
use super::data::Data;


/* Expose routes for mounting during launch */
pub fn routes() -> Vec<rocket::Route> {
    rocket::routes![hello, post]
}


/* Placeholder GET request handler to print 'Hello world!' */
#[rocket::get("/")]
fn hello() -> String {
    String::from("Hello world!")
}


/* Post request handler to accept parser info */
#[post("/remote", format = "application/json", data = "<data>")] 
async fn post(data: Json<Data>, state: &rocket::State<Mutex<ParserInfo>>) -> http::Status {
    /* Deserialise and unwrap json data */
    let Data { input, tree } = data.into_inner(); 

    /* Acquire the mutex to modify state */
    match state.lock() {
        /* If successful, update state and return ok http status */
        Ok(mut state) => {
            state.set_input(input);
            state.set_tree(tree.into());
            http::Status::Ok
        },

        /* If unsucessful, return error http status */
        Err(_) => http::Status::Conflict
    }
}



#[cfg(test)]
mod test {

    /* Request unit testing */
    use rocket::http;
    use crate::server::test::tracked_client;
    
    #[test]
    fn get_responds_hello_world() {
        /* Launch rocket client via a blocking, tracked Client for debugging */
        let client = tracked_client();

        /* Perform GET request to index route '/' */
        let response = client.get(rocket::uri!(super::hello)).dispatch();
        
        /* Assert GET request was successful and payload was correct */
        assert_eq!(response.status(), http::Status::Ok);
        assert_eq!(response.into_string().expect("'hello' response payload was not string"), "Hello world!");
    }

    #[test]
    fn unrouted_get_fails() {
        /* Launch rocket client via a blocking, tracked Client for debugging */
        let client = tracked_client();

        /* Perform GET request to non-existent route '/hello' */
        let response = client.get("/hello").dispatch();
        
        /* Assert GET request was unsuccessful with status 404 */
        assert_eq!(response.status(), http::Status::NotFound);
    }

    
    #[test]
    fn post_succeeds() {
        let client = tracked_client();

        /* Perform POST request to '/remote' */
        let response = client.post(rocket::uri!(super::post))
            .header(http::ContentType::JSON)
            .body(r#"{"input": "this is the parser input", "tree": {}}"#)
            .dispatch();

        /* Assert that POST succeeded */
        assert_eq!(response.status(), http::Status::Ok);
    }

    #[test]
    fn empty_post_fails() {
        let client = tracked_client();

        /* Perform POST request to '/remote' */
        let response = client.post(rocket::uri!(super::post))
            .header(http::ContentType::JSON)
            .body("{}")
            .dispatch();
    
        /* Assert that POST failed */
        assert_eq!(response.status(), http::Status::UnprocessableEntity);
    }

    #[test]
    fn bad_format_post_fails() {
        let client = tracked_client();

        /* Perform POST request to '/remote' */
        let response = client.post(rocket::uri!(super::post))
            .header(http::ContentType::Text) /* Incompatible header type */
            .body("Hello world")
            .dispatch();
    
        /* Assert that POST failed */
        assert_eq!(response.status(), http::Status::NotFound);
    }

    #[test]
    fn get_on_remote_fails() {
        let client = tracked_client();

        /* Perform GET request to '/remote' */
        let response = client.get(rocket::uri!(super::post)).dispatch();
    
        /* Assert that GET failed due to no found GET handlers */
        assert_eq!(response.status(), http::Status::NotFound);
    }

}