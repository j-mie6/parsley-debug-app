package view

import com.raquo.laminar.codecs.*
import com.raquo.laminar.api.L.*

import org.scalajs.dom

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Try, Success, Failure}

import model.Page

import controller.State
import controller.Tauri

val gridTemplateColumns: StyleProp[String] = styleProp("grid-template-columns")

/**
  * The DebugViewPage class represents the main page of the application, 
  * containing both the tree and source view tabs, title and github / 
  * light & dark mode buttons.
  */
abstract class DebugViewPage extends Page {
    // Bootstrap icons used on the page (https://icons.getbootstrap.com/).
    private lazy val treeIcon: Element = i(className := "bi bi-tree-fill", fontSize.px := 30)
    private lazy val fileIcon: Element = i(className := "bi bi-file-earmark-text-fill", fontSize.px := 25)
    private lazy val gitIcon: Element = i(className := "bi bi-github", fontSize.px := 40)
    private lazy val sunIcon: Element = i(className := "bi bi-brightness-high-fill", fontSize.px := 33)
    private lazy val moonIcon: Element = i(className := "bi bi-moon-fill", fontSize.px := 33)

    // Left section of the page header, containing tree and source view tabs.
    private lazy val headerLeft: Element = div(
        className := "debug-view-header-left",
        div(
            className := "debug-view-header-left-container",
            treeIcon,
            h2("Tree View", marginLeft.px := 7)
        ),
        div(className := "debug-view-header-left-vbar"),
        div(
            className := "debug-view-header-left-container",
            fileIcon,
            h2("Input View", marginLeft.px := 12)
        )
    )

    // Title element.
    private lazy val title: Element = div(
        className := "debug-view-header-title",
        h1("Dill", fontSize.px := 40, margin.px := 0)
    )

    // Right section of the page header, containing light mode button & github link.
    private lazy val headerRight: Element = div(
        className := "debug-view-header-right",
        div(
            child <-- State.isLightMode.signal
                .map((project: Boolean) => if project then moonIcon else sunIcon),
            cursor.pointer,
            alignContent.center,
            marginRight.px := 20,
            onClick --> {_ => State.toggleTheme()}
        ),
        a(href := "https://github.com/j-mie6/parsley-debug-app", target := "_blank", gitIcon)
    )

    // The page header.
    private lazy val headerView: Element = headerTag(
        className := "debug-view-header",

        headerLeft,
        title,
        headerRight,
    )

    /**
      * Render the DebugViewPage header and a child element. This allows different views to 
      * be inserted.
      *
      * @param child The debug view to render.
      * 
      * @return HTML element of the DebugView page.
      */
    override def render(child: Option[HtmlElement]): HtmlElement = super.render(Some(mainTag(
        className := "debug-view-page",

        headerView,
        child.getOrElse(div())
    )))
}
