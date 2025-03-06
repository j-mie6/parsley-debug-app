use std::fs::{self, File};
use std::io::Write;
use std::io;

use crate::events::Event;
use crate::state::{StateError, StateManager};

use crate::AppState;

#[derive(Debug, serde::Serialize)]
pub enum SourceError {
    FileNotFound,
    EventEmitError,
}

#[tauri::command]
pub fn request_source_file(state: tauri::State<AppState>, file_path: String) -> Result<(), SourceError> {
    println!("{}", file_path);

    let file_contents: Result<String, io::Error> = fs::read_to_string(file_path);

    file_contents
        .map(|contents| state.emit(Event::SourceFile(&contents)))
        .map_err(|_| SourceError::FileNotFound)
        .and_then(|result| result.map_err(|_| SourceError::EventEmitError))
}
