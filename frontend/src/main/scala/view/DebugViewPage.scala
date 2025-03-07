package view


import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration
import scala.util.{Try, Success, Failure}
import scala.scalajs.js.timers._

import com.raquo.laminar.api.L.*
import com.raquo.laminar.codecs.*
import org.scalajs.dom

import model.Page
import view.SettingsView
import view.StateManagementView
import controller.AppStateController
import controller.errors.ErrorController
import controller.viewControllers.InputViewController
import controller.viewControllers.MainViewController
import controller.viewControllers.MainViewController.View
import controller.viewControllers.SettingsViewController
import controller.viewControllers.StateManagementViewController
import controller.viewControllers.TabViewController
import controller.viewControllers.TreeViewController
import controller.tauri.Tauri
import controller.errors.ErrorController


val gridTemplateColumns: StyleProp[String] = styleProp("grid-template-columns")

/**
  * The DebugViewPage class represents the main page of the application, 
  * containing both the tree and input view tabs, title and github / 
  * light & dark mode buttons.
  */
abstract class DebugViewPage extends Page {
    private lazy val gitIcon: HtmlElement = i(className := "bi bi-github", fontSize.px := 40)

    /* Title element */
    private lazy val title: HtmlElement = div(
        className := "debug-view-header-title",
        h1("Dill", fontSize.px := 40, margin.px := 0)
    )

    /* Export tree button */
    val downloadButton: HtmlElement = button(
        className := "debug-view-button debug-view-button-download",

        /* Save button icon */
        i(className := "bi bi-floppy"),

        /* Exports current tree */
        onClick(_
            .compose(event => event.sample(TabViewController.getSelectedTab))
            .flatMapSwitch(TabViewController.getFileName)
            .flatMapMerge(TreeViewController.downloadTree)
            .collectLeft
        ) --> ErrorController.setError,
    )

    /* Uploading json element */
    val uploadButton: HtmlElement = label(
        className := "debug-view-button debug-view-button-upload",
        i(className := "bi bi-cloud-download"),
        input(
            typ := "file",
            display := "none",
            multiple := true,

            /* Triggers the file input */
            onChange.map(ev => loadFile(ev.target.asInstanceOf[dom.html.Input])) --> Observer.empty
            
        )
    )

    /* Reads contents of file and passes them to backend */
    private def loadFile(input: dom.html.Input): Unit = {
        val files = input.files
        for (i <- 0 until files.length) {
            val file = files(i)
            val reader = new dom.FileReader()
            reader.onload = _ => {
                val content = reader.result.asInstanceOf[String]
                TreeViewController.importTree.tupled((file.name, content)) --> Observer.empty
            }
            
            reader.readAsText(file)
        }
        /* Resets value so we can import the same file multiple times */
        input.value = ""
    }
                
    /* Overview information tab. */
    private lazy val infoButton: HtmlElement = button(
        className := "debug-view-button debug-view-button-info",
        i(className := "bi bi-info-circle-fill"),
    )

    private lazy val stateButton: HtmlElement = button(
        className := "debug-view-button debug-view-button-state",
        i(className:= "bi bi-sliders"),
        onClick --> (_ => 
            StateManagementViewController.toggleOpenState()
        )
    )

    /* Opener for the settings tab. */
    private lazy val settingsTabButton: HtmlElement = button(
        className := "debug-view-button debug-view-button-settings",
        i(className := "bi bi-gear-wide-connected"),
        onClick --> (_ => 
            SettingsViewController.toggleOpenSettings()
        )
    )


    /* Fast forward icon for skipping */
    private lazy val breakpointSkipIcon: Element = i(className := "bi bi-fast-forward-fill")

    /* Adds ability to skip the current breakpoint. */
    private lazy val breakpointSkipButton: Element = button(
        className := "debug-view-button debug-view-button-breakpoint-skip",
        breakpointSkipIcon, /* Fast forward icon */

        onClick.compose(_
            .sample(TreeViewController.getSessionId)
            .compose(TreeViewController.skipBreakpoints)
            .collectLeft) --> ErrorController.setError
    )

    /**
     * Semaphore controlling expansion of the descriptions.
     * 
     * A value > 0 represents expanded.
     */
    private val viewCloseSemaphore: Var[Int] = Var(0)
    private val viewCloseSemaphoreIncrement: Observer[Int] = viewCloseSemaphore.updater((x, add) => x + add)
    private val viewCloseSemaphoreDecrement: Observer[Int] = viewCloseSemaphore.updater((x, sub) => x - sub)

    /* Button used to toggle the theme */
    private lazy val themeButton: Element = div(
        cursor.pointer,
        alignContent.center,
        marginRight.px := 20,
        fontSize.px := 33,

        /* Render moonIcon in light mode; sunIcon in dark mode */
        child <-- AppStateController.getThemeIcon, 

        onClick.mapToUnit --> AppStateController.toggleTheme()
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
            themeButton,
            githubButton,
        )
    )

    /* The page header */
    private lazy val headerView: HtmlElement = headerTag(
        className := "debug-view-header",

        div(),
        title,
        headerRight,
    )

    /* Delays for the button bar expanding */
    private final val mouseEnterOpenDelay = 500
    private final val mouseLeaveCloseDelay = 200

    /* Button bar internal to the view. */
    private lazy val buttonBar: HtmlElement = div(
        className := "debug-view-left-button-bar",
        /* Button bar left. */
        div(
            display.flex,
            alignItems.center,

            onMouseEnter.mapTo(2) --> viewCloseSemaphoreIncrement,
            onMouseEnter(_.delay(mouseEnterOpenDelay).mapTo(1)) --> viewCloseSemaphoreDecrement,
            onMouseLeave(_.delay(mouseLeaveCloseDelay).mapTo(1)) --> viewCloseSemaphoreDecrement,

            settingsTabButton,
            MainViewController.renderViewButton(View.Tree, viewCloseSemaphore),
            MainViewController.renderViewButton(View.Input, viewCloseSemaphore),
            MainViewController.renderViewButton(View.Code, viewCloseSemaphore),
        ),
        /* Button bar right. */
        div(
            className := "debug-view-right-button-bar",
            child(
                div(
                    breakpointSkipButton
                )
            ) <-- TreeViewController.isDebuggingSession,
            /* Render download button */
            child(downloadButton) <-- TreeViewController.treeExists,
            uploadButton,
            stateButton,
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
    override def render(childElem: Option[HtmlElement]): HtmlElement = {
        super.render(Some(mainTag(
            className := "debug-view-page",
            headerView,
            div(
                className := "debug-view-body", 
                child(SettingsView()) <-- SettingsViewController.isSettingsOpen,
                child(StateManagementView()) <-- StateManagementViewController.isStateOpen,
                div(
                    className := "tab-and-tree-view-container",
                    cls("left-compressed") <-- SettingsViewController.isSettingsOpen,
                    cls("right-compressed") <-- StateManagementViewController.isStateOpen,

                    div(
                        className := "tab-view-container",
                        TabView()
                    ),
                    div(
                        className := "tree-view-page",
                        buttonBar,
                        cls("highlight-debug-session") <-- TreeViewController.isDebuggingSession,
                        childElem.getOrElse(div())
                    )
                )
            )
        )))
    } 
}
