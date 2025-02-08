package model

import scala.util.{Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global

import com.raquo.laminar.api.L.*

import model.DebugNode
import controller.{Tauri, DebugTreeHandler}

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
    def resetChildren(): Unit = { 
      children.set(Nil)
    }
  
    /**
      * Query the Tauri backend to get the children of this node.
      */
    def reloadChildren(): Unit = {
        for {
          nodesString: String <- Tauri.invoke[String]("fetch_node_children", Map("nodeId" -> debugNode.nodeId))
        } do {
            if (!nodesString.isEmpty) {
                children.set(
                    DebugTreeHandler.decodeDebugNodes(nodesString) match {
                        case Success(nodes) => nodes
                        case Failure(exception) => {
                          println(s"Error in decoding debug tree: ${exception.getMessage()}") 
                          Nil
                        }
                    }
                )
            }
        }
    }
}

/**
  * Companion object for a ReactiveNode.
  */
object ReactiveNode {
    def apply(debugNode: DebugNode): ReactiveNode = ReactiveNode(debugNode, Var(Nil))
}
