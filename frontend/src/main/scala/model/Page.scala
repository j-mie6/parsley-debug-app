package model

import com.raquo.laminar.api.L.*
import scala.util.Failure
import scala.util.Success
import com.raquo.laminar.api.L
import org.scalajs.dom.console

/**
  * Base class of the Dill Page.
  */
sealed abstract class BasePage {
    /**
      * Render a page.
      *
      * @param child The content to be rendered on the page.
      * @return
      */
    def render(child: Option[HtmlElement]): HtmlElement
    final def render(): HtmlElement = render(None)
}

/**
  * Abstract class for a Dill Page.
  */
abstract class Page extends BasePage {
    /**
      * Render a page.
      *
      * @param child The content to be rendered on the page.
      * @return
      */
    override def render(child: Option[HtmlElement]): HtmlElement = child match {
        case None => div()
        case Some(child) => child
    }
}
