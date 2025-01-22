mod launch;
mod data;
mod request;

pub use launch::launch;


#[cfg(test)]
mod test {
    
    use rocket::{http, local::blocking};
    use super::launch::{self, MockHandle};
    use super::request;
    
    /* Server integration testing */
    
    /* Start a blocking, tracked client for rocket */
    pub fn tracked_client(routes: Vec<rocket::Route>) -> blocking::Client {
        let mock = MockHandle::new();
        let ctx = MockHandle::routes_context();
        ctx.expect().returning(move || routes.clone());

        blocking::Client::tracked(launch::build(mock))
            .expect("Rocket failed to initialise")
    }

    #[test]
    fn server_handles_one_request() {
        let client: blocking::Client = tracked_client(rocket::routes![request::get_hello]);

        /* Format GET request to index route '/' */
        let get_request = client.get("/");
        assert_eq!(get_request.clone().dispatch().status(), http::Status::Ok);
    }
    
    #[test]
    fn server_handles_many_requests() {
        let client: blocking::Client = tracked_client(rocket::routes![request::get_hello]);
        
        /* Format GET request to index route '/' */
        let get_request = client.get("/");
        
        // TODO: Mock POST somehow

        // /* Format POST requests to route '/api/remote' */
        // let post_request = client.post("/api/remote")
        //     .header(http::ContentType::JSON)
        //     .body(r#"{"input": "this is the parser input", "tree": "tree"}"#);
        
        
        /* Repeatedly perform requests */
        for _ in 0..100 {
            assert_eq!(get_request.clone().dispatch().status(), http::Status::Ok);
            // assert_eq!(post_request.clone().dispatch().status(), http::Status::Ok);
        }
    }   
    
}