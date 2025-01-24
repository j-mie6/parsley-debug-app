mod launch;
mod parsley_tree;
mod request;

pub use launch::launch;


#[cfg(test)]
pub mod test {
    
    use rocket::{http, local::blocking};
    use mockall::predicate;

    use crate::state::{MockStateManager, StateHandle};
    use super::{launch, parsley_tree::test::RAW_TREE_SIMPLE};
    use super::request::test::test_tree;
    
    /* Server integration testing */
    
    /* Start a blocking, tracked client for rocket
       The mock should already be set with expectations */
    pub fn tracked_client(mock: MockStateManager) -> blocking::Client {
        let handle = StateHandle::new(mock);
        blocking::Client::tracked(launch::build(handle)).expect("Could not launch rocket")
    }

    #[test]
    fn server_handles_many_requests() {
        let mut mock = MockStateManager::new();

        mock.expect_set_tree().with(predicate::eq(test_tree())).times(100).return_const(());

        let client: blocking::Client = tracked_client(mock);
        
        /* Format GET request to index route '/' */
        let get_request = client.get("/");
        
        /* Format POST requests to route '/api/remote' */
        let post_request = client.post("/api/remote")
            .header(http::ContentType::JSON)
            .body(RAW_TREE_SIMPLE);
        
        
        /* Repeatedly perform requests */
        for _ in 0..100 {
            assert_eq!(get_request.clone().dispatch().status(), http::Status::Ok);
            assert_eq!(post_request.clone().dispatch().status(), http::Status::Ok);
        }
    }
}
