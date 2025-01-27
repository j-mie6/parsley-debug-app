package pages

import com.raquo.laminar.codecs.*
import com.raquo.laminar.api.L.*

import org.scalajs.dom

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Try, Success, Failure}

import lib.Tauri

val gridTemplateColumns: StyleProp[String] = styleProp("grid-template-columns")

abstract class DebugViewPage extends Page {
    private lazy val treeIcon: Element = i(className := "bi bi-tree-fill", fontSize.px := 30)
    private lazy val fileIcon: Element = i(className := "bi bi-file-earmark-text-fill", fontSize.px := 25)
    private lazy val gitIcon: Element = i(className := "bi bi-github", fontSize.px := 40)
    private lazy val sunIcon: Element = i(className := "bi bi-brightness-high-fill", fontSize.px := 40)
    private lazy val moonIcon: Element = i(className := "bi bi-moon-fill", fontSize.px := 40)

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
            // background := "#96DEC4",
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

    lazy val page: Element = mainTag(
        // Styles
        boxSizing := "border-box",
        padding.px := 30,
        
        // background := "#242526",
        // color := "#96DEC4",

        width.vw := 100,
        height.vh := 100,

        overflow.hidden,

        // Elements
        headerView
    )

    override def render(child: Option[HtmlElement]): HtmlElement = super.render(Some(mainTag(
        // Styles
        boxSizing := "border-box",
        padding.px := 30,
        
        // background := "#242526",
        color := "#96DEC4",

        width.vw := 100,
        height.vh := 100,

        overflow.hidden,

        // Elements
        headerView,
        child.getOrElse(div())
    )))
}