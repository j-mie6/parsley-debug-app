use crate::{state::StateError, trees::DebugTree};

/* Event enum representing an event fired and managed by State */
pub enum Event<'a> {
    TreeReady(&'a DebugTree) /* Tree is ready for loading in frontend */
}

impl Event<'_> {
    
    /* Get string name of the event */
    pub fn name(&self) -> String {
        match self {
            Event::TreeReady(_) => "tree-ready",
        }.to_string()
    }

    /* Serialise and return enum payload */
    pub fn payload(self) -> Result<String, EventError> {
        match self {
            Event::TreeReady(tree) => serde_json::to_string(tree),
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