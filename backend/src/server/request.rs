use rocket::{get, http, post, serde::json::Json};

use super::parsley_tree::ParsleyTree;
use crate::trees::DebugTree;
use crate::state::{StateError, StateHandle, StateManager};

/* Length of input slice returned in post response */
const RESPONSE_INPUT_LEN: usize = 16;

/* Expose routes for mounting during launch */
pub fn routes() -> Vec<rocket::Route> {
    rocket::routes![get_index, get_tree, post_tree]
}

/* Placeholder GET request handler to print 'Hello world!' */
#[get("/")]
fn get_index() -> String {
    String::from("DILL: Debugging Interactively for the ParsLey Language")
}

/* Post request handler to accept debug tree */
#[post("/api/remote/tree", format = "application/json", data = "<data>")]
fn post_tree(
    data: Json<ParsleyTree>,
    state: &rocket::State<StateHandle>,
) -> (http::Status, String) {
    /* Deserialise and unwrap json data */
    let parsley_tree: ParsleyTree = data.into_inner();
    let debug_tree: DebugTree = parsley_tree.into();

    /* Format informative response for RemoteView */
    let input: &str = debug_tree.get_input();
    let response: String = format!(
        "Posted parser tree handling input: \"{}{}\" to Dill",
        /* Include first few chars of input */
        &input[..std::cmp::min(input.len(), RESPONSE_INPUT_LEN)],
        if input.len() > RESPONSE_INPUT_LEN {
            "..."
        } else {
            ""
        },
    );

    /* Update state with new debug_tree and return response */
    match state.set_tree(debug_tree) {
        Ok(()) => (http::Status::Ok, response),
        
        Err(StateError::LockFailed) => 
            (http::Status::InternalServerError, String::from("Locking state mutex failed - try again")), 
            
        Err(_) => panic!("Unexpected error on set_tree"),
    }

}

/* Return posted DebugTree as JSON string */
#[get("/api/remote/tree")]
fn get_tree(state: &rocket::State<StateHandle>) -> String {
    match &state.get_tree() {
        Ok(tree) => serde_json::to_string_pretty(tree)
            .unwrap_or(String::from("Could not serialise tree to JSON")),
        Err(err) => format!("{:?}", err),
    }
}

#[cfg(test)]
pub mod test {

    use mockall::predicate;
    use rocket::{http, local::blocking};

    use crate::server::test::tracked_client;
    use crate::server::parsley_tree::test::RAW_TREE_SIMPLE;
    use crate::state::MockStateManager;
    use crate::trees::{DebugNode, DebugTree};

    pub fn test_tree() -> DebugTree {
        DebugTree::new(
            String::from("Test"),
            DebugNode::new(
                0u32,
                String::from("Test"),
                String::from("Test"),
                true,
                Some(0),
                String::from("Test"),
                vec![],
            ),
        )
    }

    /* Request unit testing */

    #[test]
    fn get_responds_onboarding() {
        let mock = MockStateManager::new();
        let client: blocking::Client = tracked_client(mock);

        /* Perform GET request to index route '/' */
        let response: blocking::LocalResponse =
            client.get(rocket::uri!(super::get_index)).dispatch();

        /* Assert GET request was successful and ParsleyTree was correct */
        assert_eq!(response.status(), http::Status::Ok);
        assert_eq!(
            response.into_string().expect("ParsleyTree was not string"),
            "DILL: Debugging Interactively for the ParsLey Language"
        );
    }

    #[test]
    fn unrouted_get_fails() {
        let mock = MockStateManager::new();
        let client: blocking::Client = tracked_client(mock);

        /* Perform GET request to non-existent route '/hello' */
        let response: blocking::LocalResponse = client.get("/hello").dispatch();

        /* Assert GET request was unsuccessful with status 404 */
        assert_eq!(response.status(), http::Status::NotFound);
    }

    #[test]
    fn post_tree_succeeds() {
        let mut mock = MockStateManager::new();
        mock.expect_set_tree()
            .with(predicate::eq(test_tree()))
            .returning(|_| Ok(()));

        let client: blocking::Client = tracked_client(mock);

        /* Perform POST request to '/api/remote/tree' */
        let response: blocking::LocalResponse = client
            .post(rocket::uri!(super::post_tree))
            .header(http::ContentType::JSON)
            .body(&RAW_TREE_SIMPLE)
            .dispatch();

        /* Assert that POST succeeded */
        assert_eq!(response.status(), http::Status::Ok);
    }

    #[test]
    fn empty_post_fails() {
        let mock = MockStateManager::new();
        let client: blocking::Client = tracked_client(mock);

        /* Perform POST request to '/api/remote/tree' */
        let response: blocking::LocalResponse = client
            .post(rocket::uri!(super::post_tree))
            .header(http::ContentType::JSON)
            .body("{}")
            .dispatch();

        /* Assert that POST failed */
        assert_eq!(response.status(), http::Status::UnprocessableEntity);
    }

    #[test]
    fn bad_format_post_fails() {
        let mock = MockStateManager::new();
        let client: blocking::Client = tracked_client(mock);

        /* Perform POST request to '/api/remote/tree' */
        let response: blocking::LocalResponse = client
            .post(rocket::uri!(super::post_tree))
            .header(http::ContentType::Text) /* Incompatible header type */
            .body("Hello world")
            .dispatch();

        /* Assert that POST failed */
        assert_eq!(response.status(), http::Status::NotFound);
    }

    #[test]
    fn get_returns_tree() {
        let mut mock = MockStateManager::new();
        mock.expect_get_tree().returning(|| Ok(test_tree()));

        let client: blocking::Client = tracked_client(mock);

        /* Perform GET request to '/api/remote/tree' */
        let response: blocking::LocalResponse =
            client.get(rocket::uri!(super::get_tree)).dispatch();

        /* Assert that GET succeeded */
        assert_eq!(response.status(), http::Status::Ok);
    }

    #[test]
    fn get_returns_posted_tree() {
        let mut mock = MockStateManager::new();
        mock.expect_set_tree()
            .with(predicate::eq(test_tree()))
            .returning(|_| Ok(()));

        mock.expect_get_tree().returning(|| Ok(test_tree()));

        let client: blocking::Client = tracked_client(mock);

        /* Perform POST request to '/api/remote/tree' */
        let post_response: blocking::LocalResponse = client
            .post(rocket::uri!(super::post_tree))
            .header(http::ContentType::JSON)
            .body(&RAW_TREE_SIMPLE)
            .dispatch();

        /* Assert that POST succeeded */
        assert_eq!(post_response.status(), http::Status::Ok);

        /* Perform GET request to '/api/remote/tree' */
        let get_response: blocking::LocalResponse =
            client.get(rocket::uri!(super::get_tree)).dispatch();

        /* Assert that GET succeeded */
        assert_eq!(get_response.status(), http::Status::Ok);

        /* Assert that we GET the expected tree */
        let expected_tree: &str = r#"{
            "input": "Test",
            "root": {
                "nodeId": 0,
                "name": "Test",
                "internal": "Test",
                "success": true,
                "childId": 0,
                "input": "Test",
                "isLeaf": true
            }
        }"#;

        assert_eq!(
            get_response
                .into_string()
                .expect("get_info response is not a String")
                .replace(" ", ""),
            expected_tree.replace(" ", "")
        );
    }
}
