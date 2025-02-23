package view

import com.raquo.laminar.api.L.*

import model.{DebugTree, DebugNode, ReactiveNode}

import controller.Tauri

/**
  * Object containing rendering methods for the Debug Tree
  */
object DebugTreeDisplay {
    /**
      * Render the entire tree from the root node downwards.
      *
      * @param tree The debug tree to render.
      * 
      * @return HTML element representing the whole tree.
      */
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

/**
  * Object containing rendering methods for a reactive node (and children).
  */
private object ReactiveNodeDisplay {
    /**
      * Render a reactive node display. This display enables optional rendering
      * of children nodes, toggled by ReactiveNode.reloadChildren() and 
      * ReactiveNode.resetChildren().
      *
      * @param node The reactive node structure.
      * 
      * @return HTML element representing a reactive node with optional children.
      */
    def apply(node: ReactiveNode): HtmlElement = {
        val newType =  node.debugNode.internal != node.debugNode.name
        div(
            cls("debug-tree-node-type-box") := newType,
            when (newType) {
                p(className := "debug-tree-node-type-name", node.debugNode.name)
            },
    
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
}

/**
  * Object containing render methods for a single debug node.
  */
private object DebugNodeDisplay {
    /**
      * Render a debug node. This function returns an HTML element representing
      * a single node of the tree.
      *
      * @param debugNode Representation of the debug node structure.
      * @param buttons Collapse / expand buttons. They are passed in here so
      * that the onClick functions can affect the reactive node (parent) object.
      * 
      * @return HTML Element representing a debug node.
      */
    def apply(debugNode: DebugNode, buttons: HtmlElement): HtmlElement = {
        val showButtons: Var[Boolean] = Var(false)
        div(
            className := s"debug-tree-node debug-tree-node-${if debugNode.success then "success" else "fail"}",
            onMouseEnter --> { _ => showButtons.set(true)},
            onMouseLeave --> { _ => showButtons.set(false)},
            div(
                p(className := "debug-tree-node-name", debugNode.internal),
                p(fontStyle := "italic", debugNode.input)
            ),
            buttons.amend(display <-- showButtons.signal.map(if (_) "block" else "none"))
        )
    }
}
