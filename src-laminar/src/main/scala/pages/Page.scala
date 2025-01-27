package pages

import com.raquo.laminar.api.L.*
import scala.util.Failure
import scala.util.Success
import com.raquo.laminar.api.L
import org.scalajs.dom.console

// trait Page {
//     lazy val page: Element
// }

sealed abstract class BasePage {
    def render(child: Option[HtmlElement]): HtmlElement
    final def render(): HtmlElement = render(None)
}

sealed abstract class Page extends BasePage {
    override def render(child: Option[HtmlElement]): HtmlElement = child match {
        case None => div()
        case Some(child) => child
    }
}

sealed abstract class DebugViewPage extends Page {
    override def render(child: Option[HtmlElement]): HtmlElement = super.render(Some(div(
        width := "full",
        height := "full",
        background := "#232323",
        color := "#fff",
        padding := "20px",
        headerTag(
            display.flex,
            flexDirection.row,
            justifyContent.spaceBetween,
            padding := "20px",
            div("LEFT"),
            div("MIDDLE"),
            div("RIGHT"),
        ),
        child.getOrElse(div())
    )))
}

class TreeView extends DebugViewPage {
    override def render(child: Option[HtmlElement]): HtmlElement = super.render(Some(div(
        border := "2px solid #000",
        borderRadius := "40px",
        width := "full",
        height := "full",
        "TREE"
    )))
}