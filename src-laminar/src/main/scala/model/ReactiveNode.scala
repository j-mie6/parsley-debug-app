package model

import scala.util.{Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global

import com.raquo.laminar.api.L.*

import model.DebugNode
import controller.{Tauri, DebugTreeHandler}

/**
  * Wrapper for DebugNode to add reactive children
  *
  * @param debugNode holds information about debug node
  * @param children reactive children updated at runtime
  */
case class ReactiveNode(debugNode: DebugNode, children: Var[List[DebugNode]]) {
    def resetChildren(): Unit = { 
        children.set(List[DebugNode]())
    }
    
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
                            List[DebugNode]()
                        }
                    }
                )
            }
        }
    }
}


object ReactiveNode {
    def apply(debugNode: DebugNode): ReactiveNode = ReactiveNode(debugNode, Var(List[DebugNode]()))
}
