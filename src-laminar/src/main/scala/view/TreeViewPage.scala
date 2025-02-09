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
    private val displayTree: Var[HtmlElement] = Var(DebugTreeDisplay(DebugTree.Sample))
    
    private lazy val saveIcon: HtmlElement = i(className := "bi bi-download", fontSize.px := 25, marginRight.px := 10)

    private lazy val saveButton: Element = button(
        className := "tree-view-save",
        
        saveIcon,
        "Save tree",

        onClick --> { _ => TreeController.saveTree()}
    )

    var fileNames: Var[List[String]] = Var(List())

    val treeList: Var[HtmlElement] = Var(
      div(
        children <-- fileNames.signal.map(names =>
          names.map(name => button(
            name,
            onClick --> {_ => TreeController.reloadTree(name, displayTree)}
            )): Seq[HtmlElement]
        )
      )
    )

    private lazy val getButton: Element = button(
        className := "tree-view-save",
        
        "Get trees",

        onClick --> { _ => TreeController.getTrees(fileNames)}
    )

    def apply(): HtmlElement = {
        Tauri.listen[Unit]("tree-ready", {_ => TreeController.reloadTree(displayTree)})
        super.render(Some(div(
            className := "tree-view-page",

            saveButton,
            getButton,
            child <-- displayTree,
            child <-- treeList.signal
        )))
    }
}
