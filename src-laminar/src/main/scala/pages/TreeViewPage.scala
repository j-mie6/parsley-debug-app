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

def toggleTheme() = {
    println("Theme Change")
    dom.document.documentElement.setAttribute("data-theme", if dom.document.documentElement.getAttribute("data-theme") == "dark" then "light" else "dark");
}


object TreeViewPage extends DebugViewPage {
    private val displayTree: Var[HtmlElement] = Var(DebugTreeDisplay(_debug_tree_sample))

    private lazy val reloadButton: Element = button(
        border := "2px solid #2E2F30",
        borderRadius.px := 20,
        
        textAlign := "center",

        padding.em := 0.8,
        paddingLeft.vw := 2,
        paddingRight.vw := 2,

        marginBottom.em := 0.5,

        "Reload tree",
        
        onClick --> { _ => {
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
        }}
    )

    def apply(): HtmlElement = super.render(Some(div(
        boxSizing := "border-box",
        borderRadius.px := 20,

        padding.em := 1,

        overflow.auto,

        maxHeight.percent := 85,

        reloadButton,

        button(
            "Toggle Theme",
            onClick --> { _ => {
                toggleTheme()
            }}
        ),
        child <-- displayTree
    )))
}