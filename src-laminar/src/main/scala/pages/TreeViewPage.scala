package pages

import com.raquo.laminar.api.L.*

import org.scalajs.dom

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import scala.util.Success

import displays.DebugTreeDisplay

import lib.Tauri
import lib.DebugTreeHandler
import lib._debug_tree_sample


object TreeViewPage extends DebugViewPage {
    private val displayTree: Var[HtmlElement] = Var(DebugTreeDisplay(_debug_tree_sample))
    
    private lazy val reloadIcon: HtmlElement = i(className := "bi bi-arrow-clockwise", fontSize.px := 25, marginRight.px := 10)

    private def reloadTree(): Unit = 
        for {
            treeString <- Tauri.invoke[String]("fetch_debug_tree")
        } do {
            displayTree.set(
                if treeString.isEmpty then div("No tree found") else 
                    DebugTreeHandler.decodeDebugTree(treeString) match {
                        case Failure(exception) => println(s"Error in decoding debug tree : ${exception.getMessage()}"); div()
                        case Success(debugTree) => DebugTreeDisplay(debugTree)
                    }
            )
        }

    private lazy val reloadButton: Element = button(
        className := "tree-view-reload",
        
        reloadIcon,
        "Reload tree",

        onClick --> { _ => reloadTree()}
    )

    def apply(): HtmlElement = super.render(Some(div(
        className := "tree-view-page",

        reloadButton,
        child <-- displayTree
    )))
}