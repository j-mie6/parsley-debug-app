package view

import com.raquo.laminar.api.L.*

import model.{DebugTree, DebugNode, ReactiveNode}
import controller.tauri
import controller.tauri.Tauri
import controller.tauri.Command

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
        val newType: Boolean = node.debugNode.internal != node.debugNode.name
        val expanded: Boolean = node.children.now().isEmpty;
        
        div(
            cls("debug-tree-node-type-box") := newType,
            when (newType) {
                p(className := "debug-tree-node-type-name", node.debugNode.name)
            },

            div(
                className := s"debug-tree-node",
                cls("compress") <-- node.children.signal.map(_.isEmpty),
                cls("fail") := !node.debugNode.success,

                cls("leaf") := node.debugNode.isLeaf,

                div(
                    p(className := "debug-tree-node-name", node.debugNode.internal),
                    p(fontStyle := "italic", node.debugNode.input)
                ),

                //TODO: on double click, expand all

                onClick.flatMapTo(
                    if (!expanded) {
                        EventStream.fromValue(Nil)
                    } else {
                        Tauri.invoke(Command.FetchNodeChildren, Map("nodeId" -> node.debugNode.nodeId))
                            .map(_.getOrElse(Nil))
                    }
                ) --> node.children

            ),

            div(
                className := "debug-tree-node-container",
                children <-- node.children.signal.map(_.map((child) => ReactiveNodeDisplay(ReactiveNode(child))))
             ),
        )
    }
}
