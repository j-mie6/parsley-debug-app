use tauri::Manager;

mod server;
mod state;
mod commands;
mod trees;

use state::{AppState, StateHandle};

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
    
    /* Clone the app handle for use by Rocket state */
    let rocket_handle: tauri::AppHandle = app.handle().clone();

    /* Mount the Rocket server to the running instance of Tauri */
    tauri::async_runtime::spawn(async move {
        server::launch(StateHandle::new(rocket_handle))
            .await
            .expect("Rocket failed to initialise")
    });

    Ok(())
}

/* Run the Tauri app */
pub fn run() {
    /* Fix for NVidia graphics cards */
    std::env::set_var("WEBKIT_DISABLE_DMABUF_RENDERER", "1");

    tauri::Builder::default()
        .plugin(tauri_plugin_shell::init())
        .setup(setup) /* Run app setup */
        .invoke_handler(commands::handlers()) /* Expose Tauri commands to frontend */
        .run(tauri::generate_context!()) /* Start up the app */
        .expect("error while running tauri application");
}


#[cfg(test)]
mod test {

    /* Tauri integration tests */
}
