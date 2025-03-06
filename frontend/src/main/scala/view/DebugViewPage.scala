package view

import com.raquo.laminar.api.L.*
import com.raquo.laminar.codecs.*
import com.raquo.laminar.api.features.unitArrows

import org.scalajs.dom

import scala.scalajs.js.timers._
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration
import scala.util.{Try, Success, Failure}

import model.Page

import controller.AppStateController
import controller.tauri.Tauri
import controller.viewControllers.MainViewController.View
import controller.viewControllers.MainViewController
import controller.viewControllers.TabViewController
import controller.viewControllers.TreeViewController
import controller.viewControllers.InputViewController


val gridTemplateColumns: StyleProp[String] = styleProp("grid-template-columns")

/**
  * The DebugViewPage class represents the main page of the application, 
  * containing both the tree and source view tabs, title and github / 
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
        className := "debug-view-file-button",

        /* Save button icon */
        i(className := "bi bi-floppy", fontSize.px := 30),

        /* Exports current tree */
        onClick(_
            .compose(event => event.sample(TabViewController.getSelectedTab))
            .flatMapSwitch(TabViewController.getFileName).flatMapMerge(TreeViewController.downloadTree)
        ) --> Observer.empty,
    )

    /* Uploading json element */
    val uploadButton: HtmlElement = label(
        className := "debug-view-file-button",
        i(className := "bi bi-cloud-download", fontSize.px := 30),
        input(
            typ := "file",
            display := "none",
            multiple := true,

            /* Triggers the file input */
            onChange.mapToFiles --> {_.map(loadFile)}  
        )
    )

    /* Reads contents of file and passes them to backend */
    private def loadFile(file: dom.File): Unit = {
        val reader = new dom.FileReader()

        reader.onload = _ => {
            val content = reader.result.asInstanceOf[String]
            TreeViewController.importTree.tupled((file.name, content)) --> Observer.empty
        }

        reader.readAsText(file)
    }
                
    /* Overview information tab. */
    private lazy val infoButton: HtmlElement = button(
        className := "debug-view-button debug-view-button-info",
        i(className := "bi bi-info-circle-fill"),
    )

    /* Opener for the settings tab. */
    private lazy val settingsTabButton: HtmlElement = button(
        className := "debug-view-button debug-view-button-settings",
        i(className := "bi bi-gear-wide-connected", fontSize.px := 30)
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
        className := "debug-view-button-bar",
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
            display.flex,
            alignItems.center,

            /* Render download button */
            child(downloadButton) <-- TreeViewController.treeExists,
            uploadButton,
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
                cls("highlight-debug-session") <-- TreeViewController.isDebuggingSession,
                child.getOrElse(div())
            )
        )))
    } 
}
