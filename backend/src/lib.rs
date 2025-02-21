use tauri::Manager;
use tauri::RunEvent;

mod state;
mod commands;
mod events;
mod trees;
mod server;
mod files;

use state::AppState;
use server::ServerState;


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

    files::create_saved_trees_dir().expect("Error occured while making saved_trees folder");
    
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

    /* Build app using Tauri builders */
    let app = tauri::Builder::default()
        .plugin(tauri_plugin_shell::init())     /* Allow shell access from app */
        .setup(setup)                           /* Run app setup */
        .invoke_handler(commands::handlers())   /* Expose Tauri commands to frontend */
        .build(tauri::generate_context!())      /* Build the app */
        .expect("Error building Dill");

    /* Runs app with handling events */
    app.run(|_, event| {
        /* On window shutdown, remove saved_trees folder */
        if let RunEvent::ExitRequested { code: None, .. } = event {
            files::delete_saved_trees_dir().expect("Error occured cleaning saved trees");
        }
    })
}


#[cfg(test)]
mod test {

    /* Tauri integration tests */
}
