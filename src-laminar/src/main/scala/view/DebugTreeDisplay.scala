package view

import com.raquo.laminar.api.L.*

import model.{DebugTree, DebugNode, ReactiveNode}
import controller.Tauri


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
            ReactiveNodeDisplay(ReactiveNode(tree.root))
        )
    )
}


object ReactiveNodeDisplay {
    def apply(node: ReactiveNode): HtmlElement = div(
        DebugNodeDisplay(node.debugNode, 
            div(when (! node.debugNode.isLeaf) {
                div(
                    child <-- node.children.signal.map(_.isEmpty).map(
                        if (_) 
                            button("Expand", className := "debug-tree-node-button debug-tree-node-button-expand", onClick --> { _ => node.reloadChildren() }) 
                        else 
                            button("Compress", className := "debug-tree-node-button debug-tree-node-button-compress", onClick --> { _ => node.resetChildren() })
                    )
                )
            })
        ),

        div(
            className := "debug-tree-node-container",
            children <-- node.children.signal.map(_.map((child) => ReactiveNodeDisplay(ReactiveNode(child))))
        ),
    )
}


/** Renderer for single DebugNode
  * @param debugNode case class for holding debug values to be rendered
  */
object DebugNodeDisplay {
    def apply(debugNode: DebugNode, buttons: HtmlElement): HtmlElement ={
        val showButtons: Var[Boolean] = Var(false)
        div(
            className := s"debug-tree-node debug-tree-node-${if debugNode.success then "success" else "fail"}",
            onMouseEnter --> { _ => showButtons.set(true)},
            onMouseLeave --> { _ => showButtons.set(false)},
            div(
                p(fontWeight := "bold", debugNode.name, marginBottom.px := 5), 
                p(fontStyle := "italic", debugNode.input)
            ),
            buttons.amend(display <-- showButtons.signal.map(if (_) "block" else "none"))
        )
    }
}

