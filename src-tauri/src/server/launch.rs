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
        DebugTree { }
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
    use std::sync::Mutex;
    use rocket::{Rocket, Ignite, Config};
    use rocket::figment::{Figment, providers::{Format, Toml}};
    use crate::{DebugTree, ParserInfo, server::request};

    const ROCKET_CONFIG: &str = include_str!("Rocket.toml");

    /* Assert that calling the launch() function launches the Rocket server */
    #[rocket::async_test]
    async fn launch_launches_rocket() {
        /* Launch the Rocket server */
        let rocket: Result<Rocket<Ignite>, rocket::Error> = super::launch().await;

        /* Assert the Rocket server was successfully launched */
        assert!(rocket.is_ok());
    }

    /* Assert that merging the Rocket.toml config file to the default config
    ** file results in a non-default config file
    */
    #[test]
    fn check_config_changes() {

        /* Override the default config with values from Rocket.toml */
        let figment: rocket::figment::Figment = Figment::from(Config::default())
        .merge(Toml::string(ROCKET_CONFIG).nested());

        /* Assert the Rocket server was successfully built */
        let config: Config = figment.extract().expect("Failed to extract config");
        assert_ne!(Config::default(), config);
    }

    #[rocket::async_test]
    async fn check_mounted_routes() {
        /* Placeholder parser info struct */
        let parser_info: ParserInfo = ParserInfo::new(
            String::from("This is a parser input"),
            DebugTree { }
        );

        /* Override the default config with values from Rocket.toml */
        let figment: Figment = Figment::from(Config::default())
            .merge(Toml::string(ROCKET_CONFIG).nested());

        let rocket: Rocket<Ignite> = rocket::custom(figment) /* Build the Rocket server with a custom config */
            .mount("/", request::routes()) /* Mount routes to the base path '/' */
            .manage(Mutex::new(parser_info)) /* Manage the parser info as a mutex-protected state */
            .ignite().await.expect("Rocket failed to ignite");

        /* Assert the Rocket server was successfully built */
        assert_eq!(rocket.routes().count(), 2);
    }
}