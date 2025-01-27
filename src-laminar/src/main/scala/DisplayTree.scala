package displays

import com.raquo.laminar.api.L.*

import debugger.DebugTree
import displays.DisplayNode

/**
  * DisplayTree creates the HTML element to display a DebugTree
  *
  * @param debugTree a DebugTree parsed from JSON
  */
class DisplayTree(debugTree: DebugTree) {
    /* Element to render input text */
    // TODO: move to input page
    private lazy val input: Element = h1(
        textAlign := "center", 
        debugTree.input
    )

    /* Flexbox to hold DisplayTree */
    private lazy val root: Element = div(
        display := "flex",
        flexDirection := "row",
        flexWrap := "nowrap",
        flexBasis := "auto",
        justifyContent := "space-evenly",
        alignItems := "space-evenly",
        DisplayNode(debugTree.root).element
    )

    /* HTML element exposed to be rendered */
    lazy val element: Element = div(
        input,
        root
    )
}


object DisplayTree {
    // Example DisplayTree to use in testing
    final val Sample: DisplayTree = ???
}