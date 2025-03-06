use rocket::{get, post, http, serde::json::Json};

use super::ServerState;
use crate::events::Event;
use crate::trees::{DebugTree, ParsleyTree};
use crate::state::{StateError, StateManager};

/* Length of input slice returned in post response */
const RESPONSE_INPUT_LEN: usize = 16;

/* Expose routes for mounting during launch */
pub fn routes() -> Vec<rocket::Route> {
    rocket::routes![get_index, get_tree, post_tree]
}

#[derive(Debug, serde::Serialize)]
#[serde(rename_all = "camelCase")]
struct PostTreeResponse {
    message: String,
    session_id: i32,
    #[serde(skip_serializing_if = "Option::is_none")] skip_breakpoint: Option<i32>,
}

impl PostTreeResponse {
    fn new(message: &str, session_id: i32, skips: Option<i32>) -> Json<PostTreeResponse> {     
        Json(PostTreeResponse {
            message: message.to_string(),
            session_id,
            skip_breakpoint: skips,
        })
    }

    fn success_msg(input: &str) -> String {
        format!(
            "Posted parser tree handling input: \"{}{}\" to Dill",
            /* Include first few chars of input */
            &input[..std::cmp::min(input.len(), RESPONSE_INPUT_LEN)],
            if input.len() > RESPONSE_INPUT_LEN {
                "..."
            } else {
                ""
            }
        )
    }

    fn no_skips(message: &str, session_id: i32) -> Json<PostTreeResponse> {
        PostTreeResponse::new(message, session_id, None)
    }

    fn with_skips(message: &str, session_id: i32, skips: i32) -> Json<PostTreeResponse> {
        PostTreeResponse::new(message, session_id, Some(skips))
    }
}

/* Placeholder GET request handler to print 'Hello world!' */
#[get("/")]
fn get_index() -> String {
    String::from("DILL: Debugging Interactively for the ParsLey Language")
}

/* Post request handler to accept debug tree */
#[post("/api/remote/tree", format = "application/json", data = "<data>")]
async fn post_tree(data: Json<ParsleyTree>, state: &rocket::State<ServerState>) -> (http::Status, Json<PostTreeResponse>) {
    /* Deserialise and unwrap json data */
    let parsley_tree: ParsleyTree = data.into_inner();
    let is_debugging: bool = parsley_tree.is_debugging();
    let mut debug_tree: DebugTree = parsley_tree.into();

    /* Format informative response for RemoteView */
    let msg: String = PostTreeResponse::success_msg(debug_tree.get_input());

    /* Get session_id and check for previous debugging session */
    let mut session_id: i32 = debug_tree.get_session_id();

    let session_exists: bool = state.session_id_exists(session_id).expect("State error checking if session_id exists");

    /* Set new session id if session is beginning */
    if is_debugging && session_id == -1 {
        let new_session_id: i32 = state.inner().next_session_id().expect("Pretty please");
        session_id = new_session_id;
        debug_tree.set_session_id(new_session_id);
    }

    /* Only get a new tree if not debugging and no session_id */
    let new_tree: bool = !(is_debugging || session_exists);


    match state.set_tree(debug_tree).and(if new_tree { state.emit(Event::NewTree) } else { Ok(()) }) {
        Ok(()) if !is_debugging => (http::Status::Ok, PostTreeResponse::no_skips(&msg, session_id)),

        Ok(()) => match state.receive_breakpoint_skips().await {
            Some((prev_id, skips)) => (http::Status::Ok, PostTreeResponse::with_skips(&msg, prev_id, skips)),
            None => (http::Status::InternalServerError, PostTreeResponse::no_skips(&msg, session_id)),
        },

        Err(StateError::LockFailed) => 
            (http::Status::InternalServerError, PostTreeResponse::no_skips("Locking state mutex failed - try again", session_id)),

        Err(StateError::ChannelError) =>
            (http::Status::InternalServerError, PostTreeResponse::no_skips("Failed to receive value from channel - try again", session_id)),

        Err(_) => panic!("Unexpected error on post_tree"),
    }

}

/* Return posted DebugTree as JSON string */
#[get("/api/remote/tree")]
fn get_tree(state: &rocket::State<ServerState>) -> String {
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

    use crate::events::Event;
    use crate::server::test::tracked_client; 
    use crate::state::MockStateManager;
    use crate::trees::{debug_tree, parsley_tree};

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
            .with(predicate::eq(debug_tree::test::tree()))
            .returning(|_| Ok(()));
        
        mock.expect_emit().withf(|expected| &Event::NewTree == expected)
            .returning(|_| Ok(()));
        
        let client: blocking::Client = tracked_client(mock);

        /* Perform POST request to '/api/remote/tree' */
        let response: blocking::LocalResponse = client
            .post(rocket::uri!(super::post_tree))
            .header(http::ContentType::JSON)
            .body(&parsley_tree::test::json())
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
        mock.expect_get_tree().returning(|| Ok(debug_tree::test::tree()));

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
            .with(predicate::eq(debug_tree::test::tree()))
            .returning(|_| Ok(()));

        mock.expect_get_tree().returning(|| Ok(debug_tree::test::tree()));
        
        mock.expect_emit().withf(|expected| &Event::NewTree == expected)
            .returning(|_| Ok(()));

        let client: blocking::Client = tracked_client(mock);

        /* Perform POST request to '/api/remote/tree' */
        let post_response: blocking::LocalResponse = client
            .post(rocket::uri!(super::post_tree))
            .header(http::ContentType::JSON)
            .body(&parsley_tree::test::json())
            .dispatch();

        /* Assert that POST succeeded */
        assert_eq!(post_response.status(), http::Status::Ok);

        /* Perform GET request to '/api/remote/tree' */
        let get_response: blocking::LocalResponse =
            client.get(rocket::uri!(super::get_tree)).dispatch();

        /* Assert that GET succeeded */
        assert_eq!(get_response.status(), http::Status::Ok);

        /* Assert that we GET the expected tree */
        assert_eq!(
            get_response
                .into_string()
                .expect("get_info response is not a String")
                .split_whitespace()
                .collect::<String>(),
            debug_tree::test::json()
        );
    }
}
