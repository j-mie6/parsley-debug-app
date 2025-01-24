package Display

import upickle.default as up
import upickle.default.{ReadWriter => RW, macroRW}
import scala.util.{Try, Success, Failure}

object DebugTreeHandler {
    def decodeDebugTree(jsonString: String): DebugTree = {
        Try(up.read[DebugTree](jsonString)) match {
            case Success(tree) => tree
            case Failure(err) => throw new Exception(s"Error while decoding JSON: ${err.getMessage()}", err)
        }
    }
}