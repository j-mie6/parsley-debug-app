package Display

import upickle.default as up
import upickle.default.{ReadWriter => RW, macroRW}
import scala.util.{Try, Success, Failure}

class DebugTreeHandler {
    def decodeDebugTree(jsonString: String): DebugTree = {
        up.read[DebugTree](jsonString) match {
            case tree: DebugTree => tree
            case _ => throw new Exception(s"Error while decoding JSON")
        }
    }
}