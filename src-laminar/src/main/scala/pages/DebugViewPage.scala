package pages

import org.scalajs.dom

import com.raquo.laminar.codecs.*
import com.raquo.laminar.api.L.*


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
            h2("Source View", marginLeft.px := 10, marginTop.px := 0, marginBottom.px := 0)
        )
    )

    private lazy val title: Element = div(
        display.flex,
        flexDirection.row,
        justifyContent.center,
        h1("DILL", marginTop.px := 0, marginBottom.px := 0)
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
        // width.percent := 100,
        // Elements
        headerLeft,
        title,
        headerRight,
    )

    private lazy val treeView: Element = div(
        borderRadius.px := 20,
        background := "#2E2F30",
        border := "1px solid #96DEC4",
        width.percent := 100,
        height.percent := 100
    )

    lazy val page: Element = mainTag(
        // Styles
        padding.px := 30,
        background := "#242526",
        color := "#96DEC4",
        width.px := viewWidth,
        height.vh := viewHeight,
        // Elements
        headerView,
        treeView
    )
}


trait DebugView extends Page { }
