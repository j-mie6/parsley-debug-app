package Display

import com.raquo.laminar.api.L.*

/**
  * DisplayTree creates the HTML element to display a DebugTree
  *
  * @param placeholderText placeholder comment at the top of file
  * @param children list of children of display tree's root node
  */
class DisplayTree(val placeholderText: String, val children: List[DisplayTree]) {
    
    val placeholderElement: Element = p(placeholderText)

    private val debugTreeElement: Element = {
        div(
            paddingLeft := "0",
            listStyleType := "none",
            border := "2px solid tomato",
            padding := "2px",
            placeholderElement,
            ul(
                listStyleType := "none",
                children.map((t) => li(
                    verticalAlign := "top",
                    padding := "2px",
                    t.element
                ))
            )
        )
    }

    /**
      * HTLM element to display DisplayTree
      */
    def element: Element = {
        if children.isEmpty then placeholderElement else debugTreeElement
    }
}

object DisplayTree {
    // Example of a DisplayTree to use in testing
    final val SAMPLE_TREE: DisplayTree = {
        DisplayTree("root!", List(DisplayTree("alejandro", List(DisplayTree("child"))), DisplayTree("adam"), DisplayTree("kevin")))
    }
    
    // Secondary constructors
    def apply(placeholderText: String, children: List[DisplayTree]): DisplayTree = new DisplayTree(placeholderText, children)
    def apply(placeholderText: String): DisplayTree = DisplayTree(placeholderText, List[DisplayTree]())
}