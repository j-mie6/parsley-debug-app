use tauri::Manager;
use tauri::RunEvent;
use std::fs::remove_dir_all;

mod server;
mod state;
mod commands;
mod trees;

use state::AppState;
use server::ServerState;

/* Directory for saved trees. */
const SAVED_TREE_DIR : &str = "./saved_trees/";


/* Setup Tauri app */
fn setup(app: &mut tauri::App) -> Result<(), Box<dyn std::error::Error>> {
    if cfg!(debug_assertions) {
        app.handle().plugin(
            tauri_plugin_log::Builder::default()
                .level(log::LevelFilter::Info)
                .build(),
        )?;
    }

    /* Manage the app state using Tauri */
    let app_state: AppState = AppState::new(app.app_handle().clone());
    app.manage(app_state);

    /* If the folder for saved_trees does not exist, create it. */
    if !std::path::Path::new(SAVED_TREE_DIR).exists() {
        std::fs::create_dir(SAVED_TREE_DIR)?;
    }
    
    /* Clone the app handle and use to create a ServerState */
    let server_state: ServerState = ServerState::new(app.handle().clone());

    /* Mount the Rocket server to the running instance of Tauri */
    tauri::async_runtime::spawn(async move {
        server::launch(server_state)
            .await
            .expect("Rocket failed to initialise")
    });

    Ok(())
}

/* Run the Tauri app */
pub fn run() {
    /* Fix for NVidia graphics cards */
    std::env::set_var("WEBKIT_DISABLE_DMABUF_RENDERER", "1");
    /* Build app with our setup and handlers for frontend calling */
    let app = tauri::Builder::default()
        .plugin(tauri_plugin_shell::init())
        .setup(setup)
        .invoke_handler(commands::handlers())
        .build(tauri::generate_context!())
        .expect("Error building the app");

    /* Run app setting it to clean used folders on shutdown */
    app.run(|_, event| {
        /* On window shutdown, remove saved_trees folder */
        if let RunEvent::ExitRequested { code: None, .. } = event {
            clean_saved_trees();
        }
    })
}

/* Removes all saved_trees wiith the folder */
pub fn clean_saved_trees() -> () {
    remove_dir_all(SAVED_TREE_DIR).expect("Saved trees could not be cleaned");
}


#[cfg(test)]
mod test {

    /* Tauri integration tests */
}
