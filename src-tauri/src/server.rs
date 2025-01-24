mod launch;
mod parsley_tree;
mod request;

pub use launch::launch;


#[cfg(test)]
pub mod test {
    
    use rocket::{http, local::blocking};
    use mockall::predicate;

    use crate::{state::{DebugTree, MockStateManager, StateHandle}, DebugNode};
    use super::{launch, parsley_tree::test::RAW_TREE_SIMPLE};
    
    /* Server integration testing */
    
    /* Start a blocking, tracked client for rocket
       The mock should already be set with expectations */
    pub fn tracked_client(mock: MockStateManager) -> blocking::Client {
        let handle = StateHandle::new(mock);
        blocking::Client::tracked(launch::build(handle)).expect("Could not launch rocket")
    }
    
    #[test]
    fn server_handles_many_requests() {
        // TODO: make examples for testing
        let root = DebugNode::new(String::new(), String::new(), false, String::new(), 0, vec![]);
        let tree = DebugTree::new(String::new(), root);

        let mut mock = MockStateManager::new();
            mock.expect_set().with(predicate::eq(tree));
        
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
