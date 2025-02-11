package view

import com.raquo.laminar.api.L.*

import org.scalajs.dom

import controller.TabController


object ScrollableTabView {
    val inputName: Var[String] = Var("")

    /* Input for the name of the tree to be saved */
    val nameInput =
        input(
            typ := "text",
            placeholder := "Enter text...",
            controlled(
                value <-- inputName.signal,
                onInput.mapToValue --> inputName.writer
            )
        )

    /* Renders a tab button from a saved tree name */
    private def tabButton(tabTitle: String): HtmlElement = {
        button(
            className := "tab-button",

            tabTitle,
            button(
                borderColor := "transparent",
                backgroundColor := "transparent",
                i(className := "bi bi-x-circle"),
                onClick --> {_ => ??? /* TAURI COMMAND FOR DELETING FILES */}
            ),

            onClick --> {_ => TabController.loadSavedTree(tabTitle)}
        )
    }
   
    
    /* Clockwise arrow as a visual call to action for reloading save t*/
    private lazy val reloadIcon: HtmlElement = i(className := "bi bi-arrow-clockwise",
        fontSize.px := 25, marginRight.px := 10)

    /* Button which updates fileNames with json file names. */
    private lazy val reloadButton: Element = button(
        className := "reload-saved-trees-button",
        
        reloadIcon,

        onClick --> { _ => TabController.fetchSavedTreeNames()}
    )
    
    def apply(): HtmlElement = {
        div(
            className:= "scrollable-tab-bar",
            reloadButton,
                /**
                 * Renders a list of buttons which will reload to whatever tree is 
                 * pressed on
                 */ 
            children <-- TabController.getFileNames.signal.map(names =>
                names.map(name => tabButton(name)): Seq[HtmlElement]
            )
        )
    }
}