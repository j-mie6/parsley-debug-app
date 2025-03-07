mod launch;
mod request;
mod server_state;

pub use launch::launch;
pub use server_state::ServerState;

pub type TokioMutex<T> = rocket::tokio::sync::Mutex<T>;

#[cfg(test)]
pub mod test {

    use mockall::predicate;
    use rocket::{http, local::blocking};

    use super::{launch, ServerState};
    use crate::events::Event;
    use crate::state::MockStateManager;
    use crate::trees::{debug_tree, parsley_tree};

    /* Server integration testing */


    /* Start a blocking, tracked client for rocket
    The mock should already be set with expectations */
    pub fn tracked_client(mock: MockStateManager) -> blocking::Client {
        let state = ServerState::new(mock);
        blocking::Client::tracked(launch::build(state)).expect("Could not launch rocket")
    }

    #[test]
    fn server_handles_many_requests() {
        const NUM_REPEATS: usize = 1000;

        let mut mock = MockStateManager::new();

        mock.expect_set_tree()
            .with(predicate::eq(debug_tree::test::tree()))
            .times(NUM_REPEATS)
            .returning(|_| Ok(()));

        mock.expect_emit().withf(|expected| &Event::NewTree == expected)
            .returning(|_| Ok(()));

        mock.expect_get_tree()
            .times(NUM_REPEATS)
            .returning(|| Ok(debug_tree::test::tree()));

        let client: blocking::Client = tracked_client(mock);

        /* Format GET request to index route '/' */
        let get_index = client.get("/");

        /* Format POST requests to route '/api/remote/tree' */
        let post_tree = client
            .post("/api/remote/tree")
            .header(http::ContentType::JSON)
            .body(parsley_tree::test::json());

        let get_tree = client.get("/api/remote/tree");

        /* Repeatedly perform requests */
        for _ in 0..NUM_REPEATS {
            assert_eq!(get_index.clone().dispatch().status(), http::Status::Ok);
            assert_eq!(post_tree.clone().dispatch().status(), http::Status::Ok);
            assert_eq!(get_tree.clone().dispatch().status(), http::Status::Ok);
        }
    }
}
