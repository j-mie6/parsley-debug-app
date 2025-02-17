package controller.tauri

enum Event(val name: String):
    case TreeReady extends Event("tree-ready")
