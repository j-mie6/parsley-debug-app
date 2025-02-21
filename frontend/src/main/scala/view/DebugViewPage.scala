package view

import com.raquo.laminar.codecs.*
import com.raquo.laminar.api.L.*

import org.scalajs.dom

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Try, Success, Failure}

import model.Page

import controller.AppStateController
import controller.tauri.Tauri
import controller.viewControllers.MainViewController
import controller.viewControllers.TabViewController
import controller.viewControllers.TreeViewController

val gridTemplateColumns: StyleProp[String] = styleProp("grid-template-columns")

/**
  * The DebugViewPage class represents the main page of the application, 
  * containing both the tree and source view tabs, title and github / 
  * light & dark mode buttons.
  */
abstract class DebugViewPage extends Page {
    /* Bootstrap icons used on the page (https://icons.getbootstrap.com/) */
    private lazy val treeIcon: Element = i(className := "bi bi-tree-fill", fontSize.px := 30)
    private lazy val fileIcon: Element = i(className := "bi bi-file-earmark-text-fill", fontSize.px := 25)
    private lazy val gitIcon: Element = i(className := "bi bi-github", fontSize.px := 40)
    private lazy val sunIcon: Element = i(className := "bi bi-brightness-high-fill", fontSize.px := 33)
    private lazy val moonIcon: Element = i(className := "bi bi-moon-fill", fontSize.px := 33)

    /* Left section of the page header, containing tree and source view tabs */
    private lazy val headerLeft: Element = div(
        className := "debug-view-header-left",
        div(
            className := "debug-view-header-left-container",
            cls("selected") <-- MainViewController.isTreeView(true),
            treeIcon,
            h2("Tree View", marginLeft.px := 7),
            onClick --> { _ => 
                MainViewController.setIsTreeView(true) 
            }
        ),
        div(
            className := "debug-view-header-left-container",
            cls("selected") <-- MainViewController.isTreeView(false),
            fileIcon,
            h2("Input View", marginLeft.px := 12),
            onClick --> { _ => 
                MainViewController.setIsTreeView(false) 
            }
        )
    )

    /* Title element */
    private lazy val title: Element = div(
        className := "debug-view-header-title",
        h1("Dill", fontSize.px := 40, margin.px := 0)
    )

    /* Random number generator */
    private val rand = new scala.util.Random

    /* Visual call to action for user to save the current tree */
     private lazy val saveIcon: HtmlElement = i(
        className := "bi bi-floppy-fill",
        fontSize.px := 25, marginRight.px := 10
    )

    /* Adds ability to save and store current tree. */
    private lazy val saveButton: Element = button(
        className := "save-button",

        saveIcon, /* Floppy disk icon */

        onClick.mapTo(rand.nextInt(100).toString)
            .compose(TabViewController.saveTree) 
            --> TreeViewController.setTree()
    )

    /* Button used to toggle the theme */
    private lazy val themeButton: Element = div(
        cursor.pointer,
        alignContent.center,

        marginRight.px := 20, //TODO: move to css
        
        /* Render moonIcon in light mode; sunIcon in dark mode */
        child <-- AppStateController.isLightMode.signal
            .map(if (_) then moonIcon else sunIcon),

        onClick.mapToUnit --> AppStateController.toggleTheme()
    )

    /* Button that links to the 'parsley-debug-app' Github repo */
    private lazy val githubButton: Element = a(
        href := "https://github.com/j-mie6/parsley-debug-app", target := "_blank", gitIcon
    )

    /* Right section of the page header, containing various buttons */
    private lazy val headerRight: Element = div(
        className := "debug-view-header-right",
        div(
            className := "debug-view-header-right-buttons",
            saveButton,
            themeButton,
            githubButton
        )
    )

    /* The page header */
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
    override def render(child: Option[HtmlElement]): HtmlElement = {

        super.render(Some(mainTag(
            className := "debug-view-page",
            headerView,
            TabView(),
            div(
                className := "tree-view-page",
                child.getOrElse(div())
            )
        )))
    } 
}
