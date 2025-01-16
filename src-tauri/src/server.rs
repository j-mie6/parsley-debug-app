mod launch;
mod data;
mod request;

pub use launch::launch as launch;


#[cfg(test)]
mod test {
    
    /* Server integration testing */

    use rocket::local::blocking;

    /* Start a blocking, tracked client for rocket */
    pub fn tracked_client() -> blocking::Client {
        blocking::Client::tracked(super::launch::build())
            .expect("Rocket failed to initialise")
    }

}