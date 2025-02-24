package view

import com.raquo.laminar.codecs.*
import com.raquo.laminar.api.L.*
import com.raquo.laminar.api.features.unitArrows

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
    private lazy val treeIcon: HtmlElement = i(className := "bi bi-tree-fill", fontSize.px := 30)
    private lazy val fileIcon: HtmlElement = i(className := "bi bi-file-earmark-text-fill", fontSize.px := 25)
    private lazy val gitIcon: HtmlElement = i(className := "bi bi-github", fontSize.px := 40)
    private lazy val sunIcon: HtmlElement = i(className := "bi bi-brightness-high-fill", fontSize.px := 33)
    private lazy val moonIcon: HtmlElement = i(className := "bi bi-moon-fill", fontSize.px := 33)

    /* Left section of the page header, containing tree and source view tabs */
    private lazy val headerLeft: HtmlElement = div(
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
    private lazy val title: HtmlElement = div(
        className := "debug-view-header-title",
        h1("Dill", fontSize.px := 40, margin.px := 0)
    )

    /* Random number generator */
    private val rand = new scala.util.Random

    /* Adds ability to save and store current tree. */
    private lazy val saveButton: HtmlElement = button(
        className := "debug-view-button-save debug-view-button",
        i(className := "bi bi-floppy-fill"),
        
        onClick --> { _ => {
            val treeName: String = (rand.nextInt).toString()
            TabViewController.saveTree(treeName)
            TabViewController.setSelectedTab(treeName)
        }}
    )

    /* Overview information tab. */
    private lazy val infoButton: HtmlElement = button(
        // TODO: When event streams and errors are fixed, make this emit actual information.
        className := "debug-view-button debug-view-button-info",
        i(className := "bi bi-info-circle-fill"),
    )

    /* Opener for the settings tab. */
    private lazy val settingsTabButton: HtmlElement = button(
        className := "debug-view-button debug-view-button-settings",
        i(className := "bi bi-gear-wide-connected")
    )

    private val viewSelectorsExpanded : Var[Boolean] = Var(false)

    private lazy val treeViewTabButton: HtmlElement = button(
        className := "debug-view-select-button debug-view-tree-button",
        cls("selected") <-- MainViewController.isTreeView(true),

        i(className := "bi bi-tree-fill"),

        div(
            className := "debug-view-expand-button debug-view-expand-tree",
            cls("expanded") <-- viewSelectorsExpanded.signal,
            p("Tree View", marginLeft.px := 5),
        ),

        onClick --> { _ => 
            MainViewController.setIsTreeView(true) 
        },
    )

    private lazy val sourceViewTabButton: HtmlElement = button(
        className := "debug-view-select-button debug-view-source-button",
        i(className := "bi bi-file-earmark-text-fill"),
        
        cls("selected") <-- MainViewController.isTreeView(false),

        div(
            className := "debug-view-expand-button debug-view-expand-source",
            cls("expanded") <-- viewSelectorsExpanded.signal,
            p("Source View", marginLeft.px := 5)
        ),

        onClick --> { _ => 
            MainViewController.setIsTreeView(false) 
        },
    )

    private lazy val codeViewTabButton: HtmlElement = button(
        className := "debug-view-select-button debug-view-code-button",
        i(className := "bi bi-file-earmark-code-fill"),
        
        // cls("selected") <-- MainViewController.isTreeView(true),

        div(
            className := "debug-view-expand-button debug-view-expand-code",
            cls("expanded") <-- viewSelectorsExpanded.signal,
            p("Code View", marginLeft.px := 5),
        ),

        onClick --> { _ => 
            // MainViewController.setIsTreeView(true) 
        },
    )

    /* Button used to toggle the theme */
    private lazy val themeButton: HtmlElement = div(
        child <-- AppStateController.isLightMode.signal
            .map((project: Boolean) => if project then moonIcon else sunIcon),
        cursor.pointer,
        alignContent.center,
        marginRight.px := 20,
        onClick --> {_ => AppStateController.toggleTheme()}
    )

    /* Button that links to the 'parsley-debug-app' Github repo */
    private lazy val githubButton: HtmlElement = a(
        href := "https://github.com/j-mie6/parsley-debug-app", target := "_blank", gitIcon
    )

    /* Right section of the page header, containing various buttons */
    private lazy val headerRight: HtmlElement = div(
        className := "debug-view-header-right",
        div(
            className := "debug-view-header-right-buttons",
            // saveButton,
            themeButton,
            githubButton
        )
    )

    /* The page header */
    private lazy val headerView: HtmlElement = headerTag(
        className := "debug-view-header",

        // headerLeft,
        div(),
        title,
        headerRight,
    )

    /* Button bar internal to the view. */
    private lazy val buttonBar: HtmlElement = div(
        className := "debug-view-button-bar",
        /* Button bar left. */
        div(
            display.flex,
            alignItems.center,

            onMouseEnter --> viewSelectorsExpanded.set(true),
            onMouseLeave --> viewSelectorsExpanded.set(false),


            settingsTabButton,
            treeViewTabButton,
            sourceViewTabButton,
            codeViewTabButton,
        ),
        /* Button bar right. */
        div(
            display.flex,
            alignItems.center,

            saveButton,
            infoButton,
        )
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
                buttonBar,
                child.getOrElse(div())
            )
        )))
    } 
}
