package pages

import com.raquo.laminar.api.L._
import scala.util.Failure
import scala.util.Success

trait Page {
  lazy val page: Element
}