mod server;

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
		
		server::mount(); /* Start the rocket server */
		
		Ok(())
	})
	.run(tauri::generate_context!())
	.expect("error while running tauri application");
}

