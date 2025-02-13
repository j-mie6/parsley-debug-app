package error

import com.raquo.laminar.api.L.*

import model.{DebugTree, DebugNode, ReactiveNode}

import controller.Tauri

object BreakingError {
    def apply(message: String): HtmlElement = div(
        className := "tree-view-error",
        message
    )
}