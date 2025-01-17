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
pub async fn launch() -> Rocket<Ignite> {
    build().launch().await
        .expect("Rocket failed to initialise")
}


#[cfg(test)]
mod test {
    

}