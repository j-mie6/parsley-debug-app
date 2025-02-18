package controller

import scala.util.{Try, Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global

import org.scalablytyped.runtime.StringDictionary
import com.raquo.laminar.api.L.*
import upickle.default as up

import controller.tauri.{Tauri, Command}
import model.DebugTree
import view.DebugTreeDisplay


/**
  * Object containing methods for manipulating the DebugTree.
  */
object TreeController {
    /* Reactive DebugTree */
    private val treeVar: Var[Try[DebugTree]] = Var(Failure(new Error("No tree")))
    val tree: Observer[Try[DebugTree]] = treeVar.writer

    /* Convert the DebugTree into a HtmlElement */
    val treeElem: Signal[Option[HtmlElement]] = treeVar.signal.map(_.toOption.map(DebugTreeDisplay(_)))

}
