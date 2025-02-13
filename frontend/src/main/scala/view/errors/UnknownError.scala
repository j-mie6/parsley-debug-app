package error

import com.raquo.laminar.api.L.*

import model.{DebugTree, DebugNode, ReactiveNode}

import controller.Tauri

object UnknownError {
    def apply(): HtmlElement = div(
        "Something went wrong, idk what"
    )
}