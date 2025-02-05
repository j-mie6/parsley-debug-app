package view

import com.raquo.laminar.api.L.*

import model.DebugTree
import model.DebugNode

/**
  * DisplayTree creates the HTML element to display a DebugTree
  *
  * @param tree A DebugTree parsed from JSON
  */
object DebugTreeDisplay {
    def apply(tree: DebugTree): HtmlElement = div(
        className := "debug-tree-display",
        h1(
            className := "debug-tree-title",
            p("Parser Input : ", margin.px := 0, fontSize.px := 15, fontStyle.italic, fontWeight.lighter),
            tree.input
        ),
        div(
            DebugNodeDisplay(tree.root)
        )
    )
}

/** Renderer for single DebugNode
  * @param debugNode case class for holding debug values to be rendered
  */
object DebugNodeDisplay {
    def apply(debugNode: DebugNode): HtmlElement = div(
        div(
            className := s"debug-tree-node debug-tree-node-${if debugNode.success then "success" else "fail"}",
            
            div(
                p(fontWeight := "bold", debugNode.name, marginBottom.px := 5), 
                p(fontStyle := "italic", debugNode.input)
            )
        ),
        div(
            className := "debug-tree-node-container",
    
            debugNode.children.map((child) => DebugNodeDisplay(child))
        )
    )
}

