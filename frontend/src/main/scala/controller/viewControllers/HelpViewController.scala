package controller.viewControllers

import scala.scalajs.js
import scala.scalajs.js.internal.UnitOps.unitOrOps

import com.raquo.laminar.api.L.*

import model.help.*

object HelpViewController {
    private val activeSection: Var[HelpSection] = Var(BaseHelpSection)

    private val currentIndex: Var[Int] = Var(0)

    private val popupOpen: Var[Boolean] = Var(false)

    def isPopupOpen: Signal[Boolean] = popupOpen.signal

    def openPopup(): Unit = popupOpen.set(true)

    def closePopup(): Unit = {
        popupOpen.set(false)
        currentIndex.set(0)
    }

    def getActiveSection: Signal[HelpSection] = currentIndex.signal.map(getHelpSectionFromIndex)
    
    def isFirstSection: Signal[Boolean] = getActiveSection.map(_.index == 0)
    
    def isLastSection: Signal[Boolean] = getActiveSection.map(_.index == allHelpSections.length - 1)

    def getHelpSectionFromIndex(index: Int): HelpSection = allHelpSections(index)

    def nextSection(): Unit = {
        currentIndex.update(currIndex => (currIndex + 1) % allHelpSections.length)
    }

    def prevSection(): Unit = {
        currentIndex.update(currIndex => if (currIndex == 0) allHelpSections.length - 1 else currIndex - 1)
    }

    def progressPercentage: Signal[Double] = currentIndex.signal.map { index =>
        (index.toDouble / (allHelpSections.length - 1)) * 100
    }
}