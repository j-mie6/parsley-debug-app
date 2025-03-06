package controller.viewControllers

import scala.scalajs.js

import com.raquo.laminar.api.L.*
import scala.scalajs.js.internal.UnitOps.unitOrOps
import model.DebugTree


object StateManagementViewController {
    private val stateOpen: Var[Boolean] = Var(false)
    private val currTree: Var[Option[DebugTree]] = Var(None)
    private val refs: Var[Seq[(Int, String)]] = Var(Nil)
    private var localRefs: Seq[(Int, String)] = Nil

    val noReferencesError: HtmlElement = div(
            className := "state-view-error",
            "Nothing to show"
    )

    val refsEmptySignal: Signal[Boolean] = refs.signal.map(_.isEmpty)
    /**
     * Retrieves a signal indicating whether the state management panel is open.
     * 
     * @return A Signal[Boolean] representing the open state of the state management panel.
     */
    def isStateOpen: Signal[Boolean] = stateOpen.signal

    /**
     * Toggles the visibility of the state management panel.
     */
    def toggleOpenState(): Unit = stateOpen.set(!stateOpen.now())

    /**
      * Gets the current tree
      *
      * @return A signal of an option of the current debug tree
      */
    def getCurrTree: Signal[Option[DebugTree]] = currTree.signal

    /**
      * Sets the current tree
      *
      * @param An option of the current debug tree to be set
      */
    val setCurrTree: Observer[DebugTree] = currTree.someWriter

    def getNumRefs: Signal[Int] = refs.signal.map(_.length)

    def setRefs(newRefs: Seq[(Int, String)]): Unit = refs.set(newRefs)

    def getRefs: Signal[Seq[(Int, String)]] = refs.signal

    def getRefNumber(refAddr: Int): Signal[Int] = refs.signal.map(allRefs => (allRefs.map(_._1).indexOf(refAddr)) + 1)

    def getRefValue(refAddr: Int): Signal[String] = refs.signal.map(allRefs => allRefs.find((addr: Int, refValue: String) => addr == refAddr).getOrElse(-1, "")._2)

    def updateNewRefValue(refAddr: Int, newRefValue: String): Unit =
      refs.update { oldRefs =>
        oldRefs.map {
            case (addr, _) if addr == refAddr => (addr, newRefValue)
            case pair => pair                               
        }
    }

    def getLocalRefs: Seq[(Int, String)] = localRefs

    def setLocalRefs(refs: Seq[(Int, String)]): Unit =
        localRefs = refs

    def updateLocalRefValue(refAddr: Int, newRefValue: String): Unit =
        localRefs = localRefs.map {
            case (addr, _) if addr == refAddr => (addr, newRefValue)
            case pair => pair
    }

    def clearRefs(): Unit = {
        localRefs = Nil
        refs.set(Nil)
    }
}