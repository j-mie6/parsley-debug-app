mod server;
mod parser_info;

pub use parser_info::{ParserInfo, DebugTree};

#[tauri::command]
fn tree_text() -> String {
  String::from("Tree is empty")
}

pub fn run() {
  tauri::Builder::default()
    .setup(|app| {
      if cfg!(debug_assertions) {
        app.handle().plugin(
          tauri_plugin_log::Builder::default()
          .level(log::LevelFilter::Info)
          .build(),
        )?;
      }

      /* Mount the Rocket server to the running instance of Tauri */
      tauri::async_runtime::spawn(async {
        server::launch().await.expect("Rocket failed to initialise")
      });

      Ok(())
    })
    .invoke_handler(tauri::generate_handler![tree_text])
    .run(tauri::generate_context!())
    .expect("error while running tauri application");
}

#[cfg(test)]
mod test {

	/* Tauri integration tests */

}

