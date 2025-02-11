mod launch;
mod parsley_tree;
mod request;

pub use launch::launch;

#[cfg(test)]
pub mod test {

    use mockall::predicate;
    use rocket::{http, local::blocking};

    use super::request::test::test_tree;
    use super::{launch, parsley_tree::test::RAW_TREE_SIMPLE};
    use crate::state::{MockStateManager, StateHandle};

    /* Server integration testing */

    /* Start a blocking, tracked client for rocket
    The mock should already be set with expectations */
    pub fn tracked_client(mock: MockStateManager) -> blocking::Client {
        let handle = StateHandle::new(mock);
        blocking::Client::tracked(launch::build(handle)).expect("Could not launch rocket")
    }

    #[test]
    fn server_handles_many_requests() {
        const NUM_REPEATS: usize = 1000;

        let mut mock = MockStateManager::new();

        mock.expect_set_tree()
            .with(predicate::eq(test_tree()))
            .times(NUM_REPEATS)
            .returning(|_| Ok(()));

        mock.expect_get_tree()
            .times(NUM_REPEATS)
            .returning(|| Ok(test_tree()));

        let client: blocking::Client = tracked_client(mock);

        /* Format GET request to index route '/' */
        let get_index = client.get("/");

        /* Format POST requests to route '/api/remote/tree' */
        let post_tree = client
            .post("/api/remote/tree")
            .header(http::ContentType::JSON)
            .body(RAW_TREE_SIMPLE);

        let get_tree = client.get("/api/remote/tree");

        /* Repeatedly perform requests */
        for _ in 0..NUM_REPEATS {
            assert_eq!(get_index.clone().dispatch().status(), http::Status::Ok);
            assert_eq!(post_tree.clone().dispatch().status(), http::Status::Ok);
            assert_eq!(get_tree.clone().dispatch().status(), http::Status::Ok);
        }
    }
}
