package controller

import scala.scalajs.js
import scala.scalajs.js.internal.UnitOps.unitOrOps

import com.raquo.laminar.api.L.*

import model.toast.*

object ToastController {
    /* Holds current toast information */
    private val toastVar: Var[Option[Toast]] = Var(None)

    /* Used to set an toast to the var */
    def setToast(newToast: Toast): Unit = toastVar.set(Some(newToast))

    /* Get current toast in the var */
    val getToast: Signal[Option[Toast]] = toastVar.signal
    
    /* Signal containing the html element to display the current toast */
    val getToastElem: Signal[Option[HtmlElement]] = getToast.map(_.map(_.displayElement))

    /* Sets the toast var back to no toast, used when a toast is clicked to make it disappear */
    val clearToast: Observer[Unit] = Observer((_: Unit) => toastVar.set(None))
}