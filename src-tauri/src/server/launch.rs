use std::sync::Mutex;

use rocket::{Rocket, Build, Ignite, Config};
use rocket::figment::{Figment, providers::{Format, Toml}};

use crate::{DebugTree, ParserInfo};

/* Embed Rocket.toml as a string to allow post-compilation access */
const ROCKET_CONFIG: &str = include_str!("Rocket.toml");


/* Build the Rocket server */
pub fn build() -> Rocket<Build> {
    /* Placeholder parser info struct */
    let parser_info: ParserInfo = ParserInfo::new(
        String::from("This is a parser input"),
        DebugTree::default()
    );
    
    /* Override the default config with values from Rocket.toml */
    let figment: Figment = Figment::from(Config::default())
    .merge(Toml::string(ROCKET_CONFIG).nested());
    
    rocket::custom(figment) /* Build the Rocket server with a custom config */
        .mount("/", super::request::routes()) /* Mount routes to the base path '/' */
        .manage(Mutex::new(parser_info)) /* Manage the parser info as a mutex-protected state */
}

/* Launch the Rocket server */
pub async fn launch() -> Result<Rocket<Ignite>, rocket::Error> {
    build().launch().await
}



#[cfg(test)]
mod test {
    use rocket::local::blocking;
    use rocket::{Rocket, Build, Config};
    use rocket::figment::{Figment, Provider};
    use rocket::figment::providers::{self, Toml, Format};
    
    use crate::server;
    use super::ROCKET_CONFIG;
    
    /* Launch unit testing */
    
    #[test]
    fn rocket_client_launches_successfully() {
        let rocket: Rocket<Build> = super::build();
        
        /* Fails if launching rocket would fail */
        assert!(blocking::Client::tracked(rocket).is_ok())
    }

    
    #[test]
    fn user_config_is_valid() {
        /* Assert that Rocket.toml can be parsed */
        assert!(providers::Toml::string(ROCKET_CONFIG).nested().data().is_ok());
    }
    
    #[test]
    fn default_config_overridden() {
        /* Load user config from Rocket.toml */
        let user_config: providers::Data<Toml> = providers::Toml::string(ROCKET_CONFIG).nested();
        
        /* If config could be parsed */
        if let Ok(data) = user_config.data() {    
            
            /* If config file differs from default */
            if !data.is_empty() && data != Config::default().data().unwrap() {
                
                /* Override the default config with values from Rocket.toml */
                let figment: Figment = Figment::from(Config::default())
                .merge(user_config);
                
                /* Assert the config was correctly overridden */
                let config: Config = figment.extract().expect("Failed to extract config");
                assert_ne!(Config::default(), config);
            }
        }
    }

    
    #[test]
    fn num_routes_mounted_is_correct() {
        let client: blocking::Client = server::test::tracked_client();
        
        /* Assert the Rocket server was successfully built with 3 routes */
        assert_eq!(client.rocket().routes().count(), 3);
    }

    #[test]
    fn mounted_routes_correctly_named() {
        let client: blocking::Client = server::test::tracked_client();
        let routes: Vec<&str> = client.rocket()
            .routes()
            .map(|r| r.uri.as_str())
            .collect();

        /* Assert the Rocket server was built with the correct routes */
        assert!(routes.contains(&"/"));
        assert!(routes.contains(&"/api/remote"));
    }
    
}