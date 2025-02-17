package model

import scala.util.{Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global

import com.raquo.laminar.api.L.*

import model.DebugNode
import controller.DebugTreeHandler
import controller.tauri.{Tauri, Command}

/**
  * Wrapper for DebugNode to add reactive children.
  *
  * @param debugNode holds information about debug node
  * @param children reactive children updated at runtime
  */
case class ReactiveNode(debugNode: DebugNode, children: Var[List[DebugNode]]) {
    /**
      * Clear the children from this mode.
      */
    def resetChildren(): Unit = children.set(Nil)
  
    /**
      * Query the Tauri backend to get the children of this node.
      */
    def reloadChildren(): Unit = {
        Tauri.invoke[String](Command.FetchNodeChildren, Map("nodeId" -> debugNode.nodeId))
            .filterNot(_.isEmpty())
            .map(DebugTreeHandler.decodeDebugNodes(_).getOrElse(Nil))
            --> children
    }
}

/**
  * Companion object for a ReactiveNode.
  */
object ReactiveNode {
    def apply(debugNode: DebugNode): ReactiveNode = ReactiveNode(debugNode, Var(Nil))
}
