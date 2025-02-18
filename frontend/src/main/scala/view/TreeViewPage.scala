package view

import scala.util.Failure
import scala.util.Success
import scala.concurrent.Future

import org.scalajs.dom
import com.raquo.laminar.api.L.*

import model.DebugTree
import controller.{TreeController, DebugTreeHandler}
import controller.tauri.{Tauri, Event}

/**
  * Object containing rendering functions for the TreeViewPage.
  */
object TreeViewPage extends DebugViewPage {
    private lazy val saveIcon: HtmlElement = i(className := "bi bi-download", fontSize.px := 25, marginRight.px := 10)

    /* Adds ability to save and store current tree. */
    private lazy val saveButton: Element = button(
        className := "tree-view-save",
        saveIcon,
        "Save tree",
        onClick.flatMapTo(inputName.signal) --> TreeController.saveTree
    )

    /* List of file names (excluding path and ext) wrapped in Var*/
    var fileNames: Var[List[String]] = Var(Nil)

    /* Renders a list of buttons which will reload to whatever tree is pressed on */
    val treeList = div(
        children <-- fileNames.signal.map(_.map(name => button(
            name,
            //TODO: make debugTree var and update
            // onClick.flatMapTo(TreeController.loadSavedTree(name)) --> debugTree
        )))
    )

    /* Button which updates fileNames with json file names. */
    private lazy val getButton: Element = button(
        className := "tree-view-save",
        "Get trees",
        onClick.flatMapTo(TreeController.fetchSavedTreeNames()) --> fileNames
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

    private val noTreeFound: HtmlElement = div(
        className := "tree-view-error",
        "No tree found! Start debugging by attaching DillRemoteView to a parser"
    )

    def apply(): HtmlElement = {
        val (treeStream, unlisten) = Tauri.listen(Event.TreeReady)

        super.render(Some(div(
            className := "tree-view-page",
            
            treeStream --> TreeController.tree,
            child.maybe <-- TreeController.treeElem,

            /*
            saveButton, /* Used for saving a tree using the name given in nameInput */
            getButton, /* Updates list of saved tree names */
            child <-- treeList.signal, /* Renders list of tree buttons */
            nameInput, /* Input for saving tree names */
            */
            
            onUnmountCallback(_ => unlisten.get)
        )))
    }
}
