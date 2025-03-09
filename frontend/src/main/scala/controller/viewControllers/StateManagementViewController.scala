package controller.viewControllers

import scala.scalajs.js
import scala.scalajs.js.internal.UnitOps.unitOrOps

import com.raquo.laminar.api.L._

import model.DebugTree

/**
* Provides state management functions and signals for controlling 
* the display of debug tree data and references within the application.
*/
object StateManagementViewController {
    
    /**
    * A private variable holding a boolean value that indicates whether
    * the state management panel is open (true) or closed (false).
    */
    private val stateOpen: Var[Boolean] = Var(false)
    
    /**
    * A private variable storing an optional DebugTree, which represents
    * the current debug state in the application. 
    * 
    * A `None` value signifies that no debug tree is currently set.
    */
    private val currTree: Var[Option[DebugTree]] = Var(None)
    
    /**
    * A private variable maintaining a sequence of (reference address, reference value) pairs.
    * Used to populate and update reference data in the state management view.
    */
    private val refs: Var[Seq[(Int, String)]] = Var(Nil)
    
    /**
    * A private, local collection mirroring the sequence of references (`refs`).
    * Modifications made here do not automatically propagate to the reactive `refs`.
    */
    private var localRefs: Seq[(Int, String)] = Nil

    /**
    * A private, local collection mirroring the sequence of references (`refs`).
    * Modifications made here do not automatically propagate to the reactive `refs`.
    */
    private var origRefs: Seq[(Int, String)] = Nil
    
    /**
    * A signal that emits `true` when the sequence of references is empty,
    * and `false` otherwise. Useful for conditionally displaying error messages or content.
    */
    val refsEmptySignal: Signal[Boolean] = refs.signal.map(_.isEmpty)
    
    /**
    * Retrieves a signal indicating whether the state management panel is open.
    *
    * @return A signal of type `Boolean` that is `true` if the panel is open and `false` otherwise.
    */
    def isStateOpen: Signal[Boolean] = stateOpen.signal
    
    /**
    * Toggles the visibility of the state management panel between open and closed.
    */
    def toggleOpenState(): Unit = stateOpen.set(!stateOpen.now())
    
    /**
    * Obtains a signal of an optional DebugTree, representing the current debug tree.
    *
    * @return A signal of type `Option[DebugTree]`.
    */
    def getCurrTree: Signal[Option[DebugTree]] = currTree.signal
    
    /**
    * An observer that allows setting of the current debug tree in a reactive manner.
    * Sending a DebugTree to this observer updates the underlying Var.
    */
    val setCurrTree: Observer[DebugTree] = currTree.someWriter
    
    /**
    * Provides a signal of the total number of references currently tracked.
    *
    * @return A signal emitting an integer denoting the length of the reference sequence.
    */
    def getNumRefs: Signal[Int] = refs.signal.map(_.length)
    
    /**
    * Updates the reactive references (refs) variable with a new sequence of 
    * (reference address, reference value) tuples.
    *
    * @param newRefs A sequence of (Int, String) pairs to replace the existing references.
    */
    def setRefs(newRefs: Seq[(Int, String)]): Unit = refs.set(newRefs)
    
    /**
    * Retrieves a signal of the current sequence of (reference address, reference value) pairs.
    *
    * @return A signal emitting the sequence of references.
    */
    def getRefs: Signal[Seq[(Int, String)]] = refs.signal
    
    /**
    * Produces a signal that calculates the "reference number" (1-based index)
    * of a given reference address from the current list of references.
    *
    * @param refAddr The integer address of the reference whose 1-based index is required.
    * @return A signal emitting the reference's 1-based index, or 0 if not found.
    */
    def getRefNumber(refAddr: Int): Signal[Int] = {
        refs.signal.map { allRefs =>
            allRefs.map(_._1).indexOf(refAddr) + 1
        }
    }
    
    /**
    * Produces a signal that looks up the string value associated with the specified
    * reference address. If no matching entry is found, an empty string is returned.
    *
    * @param refAddr The reference address for which to retrieve the stored string.
    * @return A signal emitting the string value for the reference, or an empty string if not found.
    */
    def getRefValue(refAddr: Int): Signal[String] =
    refs.signal.map { allRefs =>
        allRefs.find { case (addr, _) => addr == refAddr }
        .getOrElse((-1, ""))
        ._2
    }
    
    /**
    * Updates the reference sequence by changing the string value of the specified reference address
    * to a new value, while leaving all other references unchanged.
    *
    * @param refAddr     The address of the reference to be modified.
    * @param newRefValue The new string value to associate with the reference.
    */
    def updateNewRefValue(refAddr: Int, newRefValue: String): Unit = {
        refs.update { oldRefs =>
            oldRefs.map {
                case (addr, _) if addr == refAddr => (addr, newRefValue)
                case pair                         => pair
            }
        }
    }
    
    /**
    * Retrieves the local (non-reactive) sequence of reference pairs. This sequence
    * is separate from the reactive `refs` and must be updated manually to remain in sync.
    *
    * @return The current local sequence of (Int, String) pairs.
    */
    def getLocalRefs: Seq[(Int, String)] = localRefs
    
    /**
    * Sets the local (non-reactive) sequence of references to a new collection
    * of (address, value) pairs, without affecting the reactive `refs` variable.
    *
    * @param refs The new sequence of references.
    */
    def setLocalRefs(refs: Seq[(Int, String)]): Unit = {
        localRefs = refs
    }

    /**
    * Retrieves the previous sequence of reference pairs. This sequence
    * is separate from the reactive `refs`.
    *
    * @return The current local sequence of (Int, String) pairs.
    */
    def getOrigRefs: Seq[(Int, String)] = origRefs
    
    /**
    * Sets the original (non-reactive) sequence of references to a new collection
    * of (address, value) pairs.
    *
    * @param refs The new sequence of references.
    */
    def setOrigRefs(refs: Seq[(Int, String)]): Unit = {
        origRefs = refs
    }

     /**
    * Looks up the original string value associated with the specified
    * reference address. If no matching entry is found, an empty string is returned.
    *
    * @param refAddr The reference address for which to retrieve the stored string.
    * @return A signal emitting the string value for the reference, or an empty string if not found.
    */
    def getOrigRefValue(refAddr: Int): String = {
        origRefs.find { case (addr, _) => addr == refAddr }
            .getOrElse((-1, ""))
            ._2
    }
    
    /**
    * Updates the local (non-reactive) references by changing the value
    * of the specified address to a new string. 
    *
    * @param refAddr     The reference address to locate in the local collection.
    * @param newRefValue The new string value to associate with that address.
    */
    def updateLocalRefValue(refAddr: Int, newRefValue: String): Unit = {
        localRefs = localRefs.map {
            case (addr, _) if addr == refAddr => (addr, newRefValue)
            case pair                         => pair
        }
    }
    
    /**
    * Clears both the local (non-reactive) references and the reactive `refs`,
    * effectively resetting all reference data.
    */
    def clearRefs(): Unit = {
        localRefs = Nil
        origRefs = Nil
        refs.set(Nil)
    }
}
