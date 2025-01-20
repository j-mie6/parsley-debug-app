package pages

import com.raquo.laminar.api.L.*

trait DebugViewPage extends Page {
  
}

object DebugViewPage extends Page {

  def apply(child: DebugView): DebugViewPage = ???
  override def render(): Element = ???
}


trait DebugView extends Page {
  
}