use tauri::Manager;
use std::sync::Mutex;

mod server;
mod state;


/* Global app state managed by Tauri */
struct AppState {
    parser: Option<state::ParserInfo>
}

impl AppState {
    /* Create new AppState with no parser */
    fn new() -> Self {
        AppState {
            parser: None,
        }
    }
}


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
    app.manage(Mutex::new(AppState::new()));
    
    /* Clone the app handle for use by Rocket state */
    let handle: tauri::AppHandle = app.handle().clone();
    
    /* Mount the Rocket server to the running instance of Tauri */
    tauri::async_runtime::spawn(async move {
        server::launch(handle).await
            .expect("Rocket failed to initialise")
    });
    
    
    Ok(())
}

/* Run the Tauri app */
pub fn run() {
    tauri::Builder::default()
        .setup(|app| setup(app)) /* Run app setup */
        .invoke_handler(tauri::generate_handler![render_debug_tree]) /* Expose render_debug_tree() to frontend */
        .run(tauri::generate_context!()) /* Start up the app */
        .expect("error while running tauri application");
}


/* Frontend-accessible debug render */
#[tauri::command]
fn render_debug_tree(state: tauri::State<Mutex<AppState>>) -> String {
    /* Acquire the state mutex to access the parser */
    let parser: &Option<state::ParserInfo> = &state.lock().expect("State mutex could not be acquired").parser;
    
    /* If parser exists, render in JSON */
    match parser {
        Some(parser) => serde_json::to_string_pretty(parser.get_tree())
            .expect("Parser info could not be serialised"),	
        None => String::from("Tree does not exist"),
    }
}



#[cfg(test)]
mod test {
    
    /* Tauri integration tests */
    
}
