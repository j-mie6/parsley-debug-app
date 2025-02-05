package view

import com.raquo.laminar.api.L.*

import org.scalajs.dom

import scala.util.Failure
import scala.util.Success

import model.DebugTree

import controller.DebugTreeHandler
import controller.TreeController


object TreeViewPage extends DebugViewPage {
    private val displayTree: Var[HtmlElement] = Var(DebugTreeDisplay(DebugTree.Sample))
    
    private lazy val reloadIcon: HtmlElement = i(className := "bi bi-arrow-clockwise", fontSize.px := 25, marginRight.px := 10)

    private lazy val reloadButton: Element = button(
        className := "tree-view-reload",
        
        reloadIcon,
        "Reload tree",

        onClick --> { _ => TreeController.reloadTree(displayTree)}
    )

    def apply(): HtmlElement = super.render(Some(div(
        className := "tree-view-page",

        reloadButton,
        child <-- displayTree
    )))
}
