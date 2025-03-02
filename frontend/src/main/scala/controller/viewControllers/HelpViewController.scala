package controller.viewControllers

import scala.scalajs.js

import com.raquo.laminar.api.L.*
import scala.scalajs.js.internal.UnitOps.unitOrOps

import model.help.*

object HelpViewController {
    private val activeSection: Var[HelpSection] = Var(BaseHelpSection)

    private val popupOpen: Var[Boolean] = Var(false)

    def isPopupOpen: Signal[Boolean] = popupOpen.signal

    def openPopup(): Unit = popupOpen.set(true)
    def closePopup(): Unit = {
        popupOpen.set(false)
        activeSection.set(BaseHelpSection)
    }

    def getActiveSection: Signal[HelpSection] = {
        activeSection.signal
    } 

    def isFirstSection: Signal[Boolean] = {
        getActiveSection.map(_.index).map(_ == 0)
    }

    def isLastSection: Signal[Boolean] = {
        getActiveSection.map(_.index).map(_ == allHelpSections.length - 1)
    }

    def nextSection(): Unit = {
        val currentIndex = activeSection.now().index
        val nextIndex = (currentIndex + 1) % allHelpSections.length
        activeSection.set(allHelpSections(nextIndex))
    }

    def prevSection(): Unit = {
        val currentIndex = activeSection.now().index
        val prevIndex = if (currentIndex == 0) allHelpSections.length - 1 else currentIndex - 1
        activeSection.set(allHelpSections(prevIndex))
    }

    def setSection(section: HelpSection): Unit = activeSection.set(section)

    def progressPercentage: Signal[Double] = activeSection.signal.map { section =>
        (section.index.toDouble / (allHelpSections.length - 1)) * 100
    }
}