package view

import com.raquo.laminar.api.L.*

import model.{DebugTree, DebugNode, ReactiveNode}

import controller.viewControllers.MainViewController
import controller.tauri.{Tauri, Command}

/**
* Object containing rendering methods for the Debug Tree Display, which is
* used to display the debug tree in the UI.
*/
object DebugTreeDisplay {
    
    /* Variable that keeps track of how much the tree has been zoomed into */
    val zoomFactor: Var[Double] = Var(1.0)
    
    val zoomSpeed: Float = -0.002   /* Speed of zooming in and out */
    val maxZoomFactor: Double = 0.5 /* Maximum zoom factor */
    val minZoomFactor: Double = 3.0 /* Minimum zoom factor */
    
    /* Updater for the zoom factor */
    val zoomUpdate = zoomFactor.updater[Double]((prev, delta) => 
        val zoomChange = 1.0 + (delta * zoomSpeed)
        (prev * zoomChange).min(minZoomFactor).max(maxZoomFactor)
    )
    
    /* Event handler for zooming in and out of the tree */
    val wheelHandler = onWheel.filter(_.ctrlKey).map { e =>
        e.preventDefault() /* Prevent default zooming */
        e.deltaY           /* Get the vertical delta of the wheel */
    } --> zoomUpdate
    
    /**
    * Render the entire tree from the root node downwards.
    *
    * @param tree The debug tree to render.
    * 
    * @return HTML element representing the whole tree.
    */
    def apply(tree: DebugTree): HtmlElement = div(
        className := "debug-tree-display zoom-container",
        
        styleAttr <-- zoomFactor.signal.map(factor => s"transform: scale($factor);"),
        wheelHandler,
        
        ReactiveNodeDisplay(ReactiveNode(tree.root)),
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
        /* True if start of a user-defined type */
        val newType: Boolean = node.debugNode.internal != node.debugNode.name
        
        div(
            /* Render a box for user-defined parser types */
            cls("debug-tree-node-type-box") := newType,
            when (newType) {
                p(className := "debug-tree-node-type-name", node.debugNode.name)
            },

            div(className := "node-line"),

            div(
                className := s"debug-tree-node",

                /* Set reactive class names */
                cls("compress") <-- node.children.signal.map(_.isEmpty),
                cls("fail") := !node.debugNode.success,
                cls("leaf") := node.debugNode.isLeaf,

                /* Render debug node information */
                div(
                    p(className := "debug-tree-node-name", node.debugNode.internal),
                    p(fontStyle := "italic", node.debugNode.input)
                ),

                /* Expand/compress node on click */
                onClick(_
                    .sample(node.children.signal)
                    .filter(_ => !node.debugNode.isLeaf)
                    .map(_.isEmpty)
                    .flatMapMerge(
                        if (_) { 
                            /* If no children, load them in */
                            Tauri.invoke(Command.FetchNodeChildren, node.debugNode.nodeId).collectRight
                        } else {
                            /* Otherwise set them to empty list */
                            EventStream.fromValue(Nil)
                        }
                    )
                ) --> node.children.writer,

            ),

            /* Flex container for rendering children */
            div(
                className := "debug-tree-node-container",
                children <-- node.children.signal.map((nodes) =>
                    nodes.map(ReactiveNode.apply andThen ReactiveNodeDisplay.apply)
                )
            ),
        )
    }

}
