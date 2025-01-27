package pages

import org.scalajs.dom
import scala.util.{Try, Success, Failure}

import com.raquo.laminar.codecs.*
import com.raquo.laminar.api.L.*
import scala.concurrent.ExecutionContext.Implicits.global
import lib.Tauri

import displays.DisplayTree
import debugger.DebugTree
import debugger.DebugTreeHandler


val gridTemplateColumns: StyleProp[String] = styleProp("grid-template-columns")

trait DebugViewPage extends Page { }

object DebugViewPage extends Page {
    private lazy val treeIcon: Element = i(className := "bi bi-tree-fill", fontSize.px := 30)
    private lazy val fileIcon: Element = i(className := "bi bi-file-earmark-text-fill", fontSize.px := 25)
    private lazy val gitIcon: Element = i(className := "bi bi-github", fontSize.px := 40)

    private lazy val viewWidth = Math.max(dom.document.documentElement.clientWidth, dom.window.innerWidth) 
    private lazy val viewHeight = Math.max(dom.document.documentElement.clientHeight, dom.window.innerHeight)

    private lazy val headerLeft: Element = div(
        display.flex,
        justifyContent.flexStart,
        alignItems.center,
        flexDirection.row,
        div(
            margin.px := 0,
            display.flex,
            flexDirection.row,
            alignItems.center,
            treeIcon,
            h2("Tree View", marginLeft.px := 10, marginTop.px := 0, marginBottom.px := 0)
        ),
        div(
            width.px := 1,
            height.px := 40,
            background := "#96DEC4",
            border := "1px solid #96DEC4",
            margin := "0 10px 0 10px"
        ),
        div(
            display.flex,
            flexDirection.row,
            alignItems.center,
            fileIcon,
            h2("Input View", marginLeft.px := 10, marginTop.px := 0, marginBottom.px := 0)
        )
    )

    private lazy val title: Element = div(
        display.flex,
        flexDirection.row,
        justifyContent.center,
        h1("Dill", marginTop.px := 0, marginBottom.px := 0)
    )

    private lazy val headerRight: Element = div(
        display.flex,
        flexDirection.rowReverse,
        alignItems.center,
        gitIcon
    )

    private lazy val headerView: Element = headTag(
        // Styles
        display.grid,
        columnCount := 3,
        gridTemplateColumns := "1fr 1fr 1fr",
        marginBottom.px := 10,

        // Elements
        headerLeft,
        title,
        headerRight,
    )

    private lazy val treeView: Element = div(
        boxSizing := "border-box",
        background := "#2E2F30",

        borderRadius.px := 20,
        border := "2px solid #96DEC4",

        padding.em := 1,
        
        child <-- displayTree
    )
    
    private val displayTree: Var[Element] = Var(warning("No tree found"))
    private def warning(text: String): Element = div(
        h1(textAlign := "center", text),
        useSampleButton
    )

    private lazy val useSampleButton: Element = button(
        border := "2px solid #2E2F30",
        backgroundColor := "#96DEC4",
        
        borderRadius.px := 20,
        
        color := "#2E2F30",
        textAlign := "center",

        padding.em := 0.8,
        paddingLeft.vw := 2,
        paddingRight.vw := 2,

        marginBottom.em := 0.5,

        float := "right",

        "Use sample tree",

        onClick --> { _ => 
            displayTree.set(DisplayTree.Sample.element)
        }
    )

    private lazy val reloadButton: Element = button(
        border := "2px solid #2E2F30",
        backgroundColor := "#96DEC4",
        
        borderRadius.px := 20,
        
        color := "#2E2F30",
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
                    if treeString.isEmpty then warning("No tree found") else 
                        DebugTreeHandler.decodeDebugTree(treeString) match {
                            case Success[DebugTree](tree) => DisplayTree(tree).element
                            case Failure(err) => warning(err.toString())
                    }
                )
            }
        }}
    )

    lazy val page: Element = mainTag(
        // Styles
        boxSizing := "border-box",
        padding.px := 30,
        
        background := "#242526",
        color := "#96DEC4",

        width.vw := 100,
        height.vh := 100,

        // Elements
        headerView,
        reloadButton,
        treeView
    )
}


trait DebugView extends Page { }
