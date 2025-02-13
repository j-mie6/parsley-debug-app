package error

import com.raquo.laminar.api.L.*

import model.{DebugTree, DebugNode, ReactiveNode}

import controller.Tauri

object SerialisationError {
    def apply(): HtmlElement = div(
        p("FILL IN WITH POPUP")
    )
}