package Display

import upickle.default as up
import upickle.default.{ReadWriter => RW, macroRW}

class DebugTreeHandler {
    def decodeDebugTree(jsonString: String): DebugTree = {
        up.read[DebugTree](jsonString) match {
            case tree: DebugTree => tree
            case null => throw new Exception("Invalid JSON")
        }
    }
}