package model

import scala.util.{Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global

import com.raquo.laminar.api.L.*

import model.DebugNode
import controller.tauri.{Command, Tauri}

import controller.errors.ErrorController

/**
  * Wrapper for DebugNode to add reactive children.
  *
  * @param debugNode holds information about debug node
  * @param children reactive children updated at runtime
  */
case class ReactiveNode(debugNode: DebugNode, children: Var[List[DebugNode]])

/**
  * Companion object for a ReactiveNode.
  */
object ReactiveNode {
    def apply(debugNode: DebugNode): ReactiveNode = ReactiveNode(debugNode, Var(Nil))
}
