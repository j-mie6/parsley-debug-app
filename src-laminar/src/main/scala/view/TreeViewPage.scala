package view

import com.raquo.laminar.api.L.*

import org.scalajs.dom

import scala.util.Failure
import scala.util.Success

import model.DebugTree

import controller.DebugTreeHandler
import controller.TreeController
import controller.Tauri


object TreeViewPage extends DebugViewPage {
    private val displayTree: Var[HtmlElement] = Var(p("No tree found - start debugging by attaching DillRemoteView to a parser"))
    
    def apply(): HtmlElement = {
        Tauri.listen[Unit]("tree-ready", {_ => TreeController.reloadTree(displayTree)})
        super.render(Some(div(
            className := "tree-view-page",
            child <-- displayTree
        )))
    }
}
