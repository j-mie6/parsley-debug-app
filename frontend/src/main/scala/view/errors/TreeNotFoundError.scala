package error

import com.raquo.laminar.api.L.*

import model.{DebugTree, DebugNode, ReactiveNode}

import controller.Tauri

object TreeNotFoundError {
    def apply(): HtmlElement = div(
        className := "tree-view-error",
        "No tree found! Start debugging by attaching DillRemoteView to a parser"
    )
}