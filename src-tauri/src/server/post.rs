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
