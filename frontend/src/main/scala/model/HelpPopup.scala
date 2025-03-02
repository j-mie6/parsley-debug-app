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
    override def visualSource: String = "assets/help/attach_parser.png"
    override def index: Int = 0
}

case object HelpAttachParser extends HelpSection {
    override def title: String = "How to Attach a Parser"
    override def infoText: String = "Get started by attaching a DillRemoteView to your Parsley parser."
    override def visualSource: String = "assets/help/attach_parser.png"
    override def index: Int = 1
}

case object HelpNavigatingUI extends HelpSection {
    override def title: String = "Navigating the UI"
    override def infoText: String = "Use the tab system to switch between different trees and configurations."
    override def visualSource: String = "assets/help/navigating_ui.png"
    override def index: Int = 2
}

case object HelpUnderstandingTrees extends HelpSection {
    override def title: String = "Understanding Parse Trees"
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