import scala.util.{Try, Success, Failure}

import org.scalajs.dom
import scala.concurrent.ExecutionContext.Implicits.global

import com.raquo.laminar.api.L.*

import lib.Tauri

import displays.DisplayTree
import pages.DebugViewPage
import debugger.DebugTreeHandler
import debugger.DebugNode

private val tree: Var[Element] = Var(p("None"))

@main def app = renderOnDomContentLoaded(
    dom.document.getElementById("app"),
    DebugViewPage.page
    // div(
    //     child <-- tree,
    //     button(
    //         onClick --> { _ => {
    //             for {
    //                 text <- Tauri.invoke[String]("tree_text")
    //             } do {
    //                 tree.set(p(text))
    //                 // DebugTreeHandler.decodeDebugTree(text) match {
    //                 //     case Success(t) => tree.set(DisplayTree.from(t.root).element())
    //                 //     case Failure(err) => ()
    //                 // }
    //             }
    //         }},
    //         "Reload tree"
    //     )
    // )
)
