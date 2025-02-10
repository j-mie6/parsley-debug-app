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

    /* Adds ability to save and store current tree. */
    private lazy val saveButton: Element = button(
        className := "tree-view-save",
        
        saveIcon,
        "Save tree",

        onClick --> { _ => TreeController.saveTree(inputName.now())}
    )

    /* List of file names (excluding path and ext) wrapped in Var*/
    var fileNames: Var[List[String]] = Var(Nil)

    /* Renders a list of buttons which will reload to 
        whatever tree is pressed on */
    val treeList: Var[HtmlElement] = Var(
      div(
        children <-- fileNames.signal.map(names =>
          names.map(name => button(
            name,
            onClick --> {_ => TreeController.reloadSavedTree(name, displayTree)}
            )): Seq[HtmlElement]
        )
      )
    )

    /* Button which updates fileNames with json file names. */
    private lazy val getButton: Element = button(
        className := "tree-view-save",
        
        "Get trees",

        onClick --> { _ => TreeController.getTrees(fileNames)}
    )

    val inputName: Var[String] = Var("")
    
    val nameInput = div(
      input(
        typ := "text",
        placeholder := "Enter text...",
        controlled(
          value <-- inputName.signal,
          onInput.mapToValue --> inputName.writer
        )
      ),
    )

    def apply(): HtmlElement = {
        Tauri.listen[Unit]("tree-ready", {_ => TreeController.reloadTree(displayTree)})
        super.render(Some(div(
            className := "tree-view-page",

            child <-- displayTree,

            /*
            saveButton, /* Used for saving a tree using the name given in nameInput */
            getButton, /* Updates list of saved tree names */
            child <-- treeList.signal, /* Renders list of tree buttons */
            nameInput, /* Input for saving tree names */
            */
        )))
    }
}
