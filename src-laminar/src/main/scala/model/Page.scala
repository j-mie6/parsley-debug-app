package model

import com.raquo.laminar.api.L.*
import scala.util.Failure
import scala.util.Success
import com.raquo.laminar.api.L
import org.scalajs.dom.console

sealed abstract class BasePage {
    def render(child: Option[HtmlElement]): HtmlElement
    final def render(): HtmlElement = render(None)
}

abstract class Page extends BasePage {
    override def render(child: Option[HtmlElement]): HtmlElement = child match {
        case None => div()
        case Some(child) => child
    }
}
