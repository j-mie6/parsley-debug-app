package controller

import upickle.default as up
import scala.util.{Try, Success, Failure}

import model.DebugTree

/**
  * DebugTreeHandler is responsible for deserialising the JSON string passed by the backend
  * into the DebugTree case class
  * 
  * decodeDebugTree:
  * @param jsonString the JSON string representing the debug tree
  * @return a Try containing either the tree or an exception 
  * 
  */
object DebugTreeHandler {
    def decodeDebugTree(jsonString: String): Try[DebugTree] = {
        Try(up.read[DebugTree](jsonString)) match {
            case tree: Success[_] => tree
            case Failure(err) => Failure(Exception(s"Error while decoding JSON: ${err.getMessage()}", err))
        }
    }

    def decodeDebugTrees(jsonString: String): Try[List[DebugTree]] = {
        Try(up.read[List[DebugTree]](jsonString)) match {
            case trees: Success[_] => trees
            case Failure(err) => Failure(Exception(s"Error while decoding JSON: ${err.getMessage()}", err))
        }
    }
}