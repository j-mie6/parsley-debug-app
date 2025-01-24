mod server;
mod debug_tree;

pub use debug_tree::{DebugTree, DebugNode};


#[tauri::command]
fn tree_text() -> String {
    String::from(r#"
    {
        "input": "Test",
        "root": {
            "name": "Test",
            "internal": "Test",
            "success": true,
            "number": 0,
            "input": "Test",
            "children": [
                {
                    "name": "Test1",
                    "internal": "Test1",
                    "success": true,
                    "number": 0,
                    "input": "Test1",
                    "children": [
                        {
                            "name": "Test1.1",
                            "internal": "Test1.1",
                            "success": true,
                            "number": 0,
                            "input": "Test1.1",
                            "children": []
                        }
                    ]
                },
                {
                    "name": "Test2",
                    "internal": "Test2",
                    "success": true,
                    "number": 0,
                    "input": "Test2",
                    "children": [
                        {
                            "name": "Test2.1",
                            "internal": "Test2.1",
                            "success": true,
                            "number": 0,
                            "input": "Test2.1",
                            "children": []
                        }
                    ]
                }
            ]
        }
    }
    "#)
    // String::from(r#"{ "name": "~>", "internal": "~>", "success": true, "input": "hello world!", "number": 0, "children": [ { "name": "~>", "internal": "~>", "success": true, "input": "hello world!", "number": 1, "children": [ { "name": "'h'", "internal": "'h'", "success": true, "input": "hello world!", "number": 1, "children": [] }, { "name": "|", "internal": "|", "success": true, "input": "hello world!", "number": 2, "children": [ { "name": "\"ello\"", "internal": "\"ello\"", "success": true, "input": "hello world!", "number": 1, "children": [] } ] } ] }, { "name": "\" world!\"", "internal": "\" world!\"", "success": true, "input": "hello world!", "number": 2, "children": [] } ] }"#)
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
