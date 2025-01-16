use std::sync::Mutex;
use rocket::{post, serde::json::Json, http};

use super::{DebugTree, ParserInfo};

#[derive(serde::Deserialize)] /* Support Json deserialisation */
struct ParsleyDebugTree { } //TODO: populate struct correctly

impl Into<DebugTree> for ParsleyDebugTree {
    fn into(self) -> DebugTree {
        DebugTree { } // TODO: convert DebugTree correctly
    }
}


#[derive(serde::Deserialize)]
struct Data {
    input: String,
    tree: ParsleyDebugTree,
}


#[allow(private_interfaces)] /* Satisfy clippy */
/* Handle a post request containing parser data */
#[post("/remote", format = "application/json", data = "<data>")] 
pub async fn post(data: Json<Data>, state: &rocket::State<Mutex<ParserInfo>>) -> http::Status {
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

    /* Post unit testing */
    use rocket::http;
    use crate::server::test::tracked_client;

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