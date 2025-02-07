package controller

import upickle.default as up
import scala.util.{Try, Success, Failure}

import model.{DebugTree, DebugNode}

/**
  * DebugTreeHandler object contains methods on decoding JSON into debug tree
  * data structures.
  */
object DebugTreeHandler {
  /**
    * Decode a JSON string into a DebugTree class.
    *
    * @param jsonString The JSON string to convert.
    * 
    * @return A DebugTree class.
    */
  def decodeDebugTree(jsonString: String): Try[DebugTree] = {
    Try(up.read[DebugTree](jsonString)) match {
      case tree: Success[_] => tree
      case Failure(err) => Failure(Exception(s"Error while decoding JSON: ${err.getMessage()}", err))
    }
  }

  
  def decodeDebugNodes(jsonString: String): Try[List[DebugNode]] = {
    Try(up.read[List[DebugNode]](jsonString)) match {
      case nodes: Success[_] => nodes
      case Failure(err) => Failure(Exception(s"Error while decoding JSON: ${err.getMessage()}", err))
    }
  }
}