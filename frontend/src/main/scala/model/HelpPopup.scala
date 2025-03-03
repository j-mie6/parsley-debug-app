package model.help

import com.raquo.laminar.api.L.*

import controller.viewControllers.HelpViewController


sealed trait HelpSection {
    def title: String
    def infoText: String
    def visualSource: String
    def index: Int
}

case object BaseHelpSection extends HelpSection {
    override def title: String = "How to Dill"
    override def infoText: String = "Getting started"
    override def visualSource: String = "assets/help/basicImage.png"
    override def index: Int = 0
}

case object HelpAttachParser extends HelpSection {
    override def title: String = "1. Attach a Parser"
    override def infoText: String = "Don't forget to use the @experimental and @parsley.debuggable tags"
    override def visualSource: String = "frontend/src/assets/help/attachParser.gif"
    override def index: Int = 1
}

case object HelpNavigatingUI extends HelpSection {
    override def title: String = "2. Navigate the debug tree"
    override def infoText: String = "Use the tab system to switch between different trees and configurations."
    override def visualSource: String = "assets/help/navigating_ui.png"
    override def index: Int = 2
}

case object HelpUnderstandingTrees extends HelpSection {
    override def title: String = "3. Understanding Parse Trees"
    override def infoText: String = "Parse trees represent the structure of your parsed input."
    override def visualSource: String = "assets/help/understanding_trees.png"
    override def index: Int = 3
}

val allHelpSections: List[HelpSection] = List(
    BaseHelpSection,
    HelpAttachParser, 
    HelpNavigatingUI, 
    HelpUnderstandingTrees
)