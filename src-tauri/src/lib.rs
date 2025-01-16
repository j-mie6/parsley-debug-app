mod server;
mod parser_info;

pub use parser_info::{ParserInfo, DebugTree};

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
		tauri::async_runtime::spawn(server::launch()); 
		
		Ok(())
	})
	.run(tauri::generate_context!())
	.expect("error while running tauri application");
}

