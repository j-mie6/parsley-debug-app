mod launch;
mod payload;
mod request;

pub use launch::launch;


#[cfg(test)]
pub mod test {
    
    use rocket::{http, local::blocking};
    use crate::state::{DebugTree, MockStateManager, ParserInfo, StateHandle};

    use mockall::predicate;

    use super::launch;
    
    /* Server integration testing */
    
    /* Start a blocking, tracked client for rocket
       The mock should already be set with expectations */
    pub fn tracked_client(mock: MockStateManager) -> blocking::Client {
        let handle = StateHandle::new(mock);
        blocking::Client::tracked(launch::build(handle)).expect("Could not launch rocket")
    }
    
    #[test]
    fn server_handles_many_requests() {
        // TODO: make DebugTree/ParserInfo examples for testing
        let debuginfo = DebugTree::new(String::new(), String::new(), false, String::new(), 0, vec![]);
        let pinfo = ParserInfo::new(String::new(), debuginfo);

        let mut mock = MockStateManager::new();
        mock.expect_set_info().with(predicate::eq(pinfo));
        
        let client: blocking::Client = tracked_client(mock);
        
        /* Format GET request to index route '/' */
        let get_request = client.get("/");
        
        /* Format POST requests to route '/api/remote' */
        let post_request = client.post("/api/remote")
            .header(http::ContentType::JSON)
            .body(r#"{"input": "this is the parser input", "tree": "tree"}"#);
        
        
        /* Repeatedly perform requests */
        for _ in 0..100 {
            assert_eq!(get_request.clone().dispatch().status(), http::Status::Ok);
            assert_eq!(post_request.clone().dispatch().status(), http::Status::Ok);
        }
    }
}
