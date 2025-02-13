package error

import com.raquo.laminar.api.L.*

import model.{DebugTree, DebugNode, ReactiveNode}

import controller.Tauri

object LockFailedError {
    def apply(): HtmlElement = div(
        p("Could not acquire lock")
    )
}