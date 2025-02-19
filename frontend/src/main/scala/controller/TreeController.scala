package controller

import com.raquo.laminar.api.L.*

import model.DebugTree
import view.DebugTreeDisplay


/**
  * Object containing methods for manipulating the DebugTree.
  */
object TreeController {

    /* Reactive DebugTree */
    private val treeVar: Var[Option[DebugTree]] = Var(None) 
    val tree: Observer[DebugTree] = treeVar.someWriter

    /* Convert the DebugTree into a HtmlElement */
    val treeElem: Signal[Option[HtmlElement]] = treeVar.signal.map(_.map(DebugTreeDisplay(_)))

}
