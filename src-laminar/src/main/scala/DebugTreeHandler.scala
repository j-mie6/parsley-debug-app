package Display

import upickle.default as up
import upickle.default.{ReadWriter => RW, macroRW}

object DebugTreeHandler {
    def decodeDebugTree(jsonString: String): DebugTree = {
        Try(up.read[DebugTree](jsonString)) match {
            case Success(tree) => tree
            case Failure(error) => throw new Exception(s"Error while decoding JSON: ${error.getMessage}", error)
        }
    }
}