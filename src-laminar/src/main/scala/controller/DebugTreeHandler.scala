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
    * @return A Try containing DebugTree class or an error message.
    */
  def decodeDebugTree(jsonString: String): Try[DebugTree] = {
    Try(up.read[DebugTree](jsonString)) match {
      case tree: Success[_] => tree
      case Failure(err) => Failure(Exception(s"Error while decoding JSON: ${err.getMessage()}", err))
    }
  }

  /**
    * Decode a JSON string into a DebugNode class.
    *
    * @param jsonString The JSON string to convert.
    * 
    * @return A Try containing DebugNode class or an error message.
    */
  def decodeDebugNodes(jsonString: String): Try[List[DebugNode]] = {
    Try(up.read[List[DebugNode]](jsonString)) match {
      case nodes: Success[_] => nodes
      case Failure(err) => Failure(Exception(s"Error while decoding JSON: ${err.getMessage()}", err))
    }
  }
}