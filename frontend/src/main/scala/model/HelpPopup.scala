package model.help

import com.raquo.laminar.api.L.*

import controller.viewControllers.HelpViewController

/**
  * HelpSection is the generic representation of each section in the help popup for Dill
  * 
  * @param title Title of the section
  * @param infoText Supplementary text that further describes an action
  * @visualSource The relative path to an image that shows the action
  * @index The index of this section amongst all help sections
  */
sealed trait HelpSection {
    def title: String
    def infoText: String
    def visualSource: String
    def index: Int
}

case object BaseHelpSection extends HelpSection {
    override def title: String = "How to Dill"
    override def infoText: String = "Getting started"
    override def visualSource: String = "frontend/styles/icons/dillIcon.png"
    override def index: Int = 0
}

case object HelpAttachParser extends HelpSection {
    override def title: String = "1. Attach a Parser"
    override def infoText: String = "Don't forget to use the @experimental and @parsley.debuggable tags."
    override def visualSource: String = "frontend/src/assets/help/attachParser.gif"
    override def index: Int = 1
}

case object HelpInputView extends HelpSection {
    override def title: String = "2. Input View"
    override def infoText: String = "Use the Input View to check thing string being parsed."
    override def visualSource: String = "frontend/src/assets/help/inputView.gif"
    override def index: Int = 2
}

case object HelpCodeView extends HelpSection {
    override def title: String = "3. Code View"
    override def infoText: String = "Use the Code View to inspect the file a parser was written in."
    override def visualSource: String = "frontend/src/assets/help/codeView.gif"
    override def index: Int = 3
}

case object HelpTraverseTrees extends HelpSection {
    override def title: String = "4. Traversing Trees"
    override def infoText: String = "Click once to expand nodes and click again to compress nodes."
    override def visualSource: String = "frontend/src/assets/help/traverseTree.gif"
    override def index: Int = 4
}

case object HelpTreeColours extends HelpSection {
    override def title: String = "5. Tree Colours"
    override def infoText: String = "For a basic Dill tree, green indicates parse success and red indicates a failure."
    override def visualSource: String = "frontend/src/assets/help/treeColours.gif"
    override def index: Int = 5
}

case object HelpIterativeNodes extends HelpSection {
    override def title: String = "6. Iterative Nodes"
    override def infoText: String = "Iterative nodes display one child by default and can be scrolled through. Clicking an iterative node expands all its children."
    override def visualSource: String = "frontend/src/assets/help/iterativeNodes.gif"
    override def index: Int = 6
}

case object HelpDownloadingTrees extends HelpSection {
    override def title: String = "7. Downloading Trees"
    override def infoText: String = "Download trees for storage and easy collaboration."
    override def visualSource: String = "frontend/src/assets/help/downloadTree.gif"
    override def index: Int = 7
}

case object HelpUploadingTrees extends HelpSection {
    override def title: String = "8. Uploading Trees"
    override def infoText: String = "Upload trees generated by any user and view them."
    override def visualSource: String = "frontend/src/assets/help/uploadTree.gif"
    override def index: Int = 8
}

case object HelpRefManagement extends HelpSection {
    override def title: String = "9. Reference Management"
    override def infoText: String = "Use the References panel to modify a parser's state when a reference is passed."
    override def visualSource: String = "frontend/src/assets/help/stateManagement.gif"
    override def index: Int = 9
}

case object HelpBreakpoints extends HelpSection {
    override def title: String = "10. Breakpoints"
    override def infoText: String = "Use breakpoints to pause execution and inspect a debug tree. Skip through various breakpoints to explore advancing outputs."
    override def visualSource: String = "frontend/src/assets/help/breakpoints.gif"
    override def index: Int = 10
}

case object HelpSummary extends HelpSection {
    override def title: String = "That's it!"
    override def infoText: String = "Debug interactively with Dill"
    override def visualSource: String = "frontend/src/assets/help/dillSummary.gif"
    override def index: Int = 11
}

val allHelpSections: List[HelpSection] = List(
    BaseHelpSection,
    HelpAttachParser, 
    HelpInputView, 
    HelpCodeView,
    HelpTraverseTrees,
    HelpTreeColours,
    HelpIterativeNodes,
    HelpDownloadingTrees,
    HelpUploadingTrees,
    HelpRefManagement,
    HelpBreakpoints,
    HelpSummary
)