package controller.tauri

enum Event(val name: String):
    case TreeReady extends Event("tree-ready")

object Event {
    given eventToString: Conversion[Event, String] with
        def apply(event: Event): String = event.name
}