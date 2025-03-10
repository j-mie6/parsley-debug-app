use crate::{state::StateError, trees::DebugTree};

/* Event enum representing an event fired and managed by State */
#[derive(Debug, PartialEq)]
pub enum Event<'a> {
    TreeReady(&'a DebugTree),   /* Tree is ready for loading in frontend */
    NewTree,                    /* New tree is sent from RemoteView */
    SourceFile(&'a String),     /* Source file requested is sent */
}

impl Event<'_> {
    
    /* Get string name of the event */
    pub fn name(&self) -> String {
        match self {
            Event::TreeReady(_) => "tree-ready",
            Event::NewTree => "new-tree",
            Event::SourceFile(_) => "upload-code-file"
        }.to_string()
    }

    /* Serialise and return enum payload */
    pub fn payload(self) -> Result<String, EventError> {
        match self {
            Event::TreeReady(tree) => serde_json::to_string(tree),
            Event::NewTree => serde_json::to_string(&()),
            Event::SourceFile(contents) => serde_json::to_string(contents),
        }.map_err(EventError::from)
    }

}

/* Errors that can occur from event payload access and serialisation */
#[derive(Debug)]
pub enum EventError {
    SerializeFailed
}

/* Event error conversions */
impl From<serde_json::Error> for EventError {
    fn from(_: serde_json::Error) -> Self {
        EventError::SerializeFailed
    }
}

impl From<EventError> for StateError {
    fn from(_: EventError) -> StateError {
        StateError::EventEmitFailed
    }
}