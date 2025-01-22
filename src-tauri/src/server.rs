mod launch;
mod payload;
mod request;

pub use launch::launch;


#[cfg(test)]
mod test {
    
    use rocket::{http, local::blocking};
    use super::launch;
    
    /* Server integration testing */
    
    /* Start a blocking, tracked client for rocket */
    pub fn tracked_client() -> blocking::Client {
        blocking::Client::tracked(launch::build(todo!("Pass Tauri AppHandle to Rocket build")))
            .expect("Rocket failed to initialise")
    }
    
    #[test]
    fn server_handles_many_requests() {
        let client: blocking::Client = tracked_client();
        
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