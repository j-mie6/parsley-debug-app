package view

import com.raquo.laminar.api.L.*

import model.{DebugTree, DebugNode, ReactiveNode}

import controller.viewControllers.MainViewController
import controller.tauri.Tauri
import controller.tauri.Command

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
        ReactiveNodeDisplay(ReactiveNode(tree.root)),
        styleAttr <-- zoomFactor.signal.map(factor => s"transform: scale($factor);"),
        wheelHandler
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
        def expanded: Boolean = node.children.now().isEmpty;
        
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
                children <-- node.children.signal.map((nodes) =>
                    nodes.map(ReactiveNode.apply andThen ReactiveNodeDisplay.apply)
                )
            ),
        )
    }
}

// /**
// * Object containing render methods for a single debug node.
// */
// private object DebugNodeDisplay {
//     /**
//     * Render a debug node. This function returns an HTML element representing
//     * a single node of the tree.
//     *
//     * @param debugNode Representation of the debug node structure.
//     * @param buttons Collapse / expand buttons. They are passed in here so
//     * that the onClick functions can affect the reactive node (parent) object.
//     * 
//     * @return HTML Element representing a debug node.
//     */
//     def apply(debugNode: DebugNode, buttons: HtmlElement): HtmlElement = {
//         val showButtons: Var[Boolean] = Var(false)
//         div(
//             className := s"debug-tree-node debug-tree-node-${if debugNode.success then "success" else "fail"}",
//             onMouseEnter --> { _ => showButtons.set(true)},
//             onMouseLeave --> { _ => showButtons.set(false)},
//             div(
//                 p(className := "debug-tree-node-name", debugNode.internal),
//                 p(fontStyle := "italic", debugNode.input)
//             ),
//             buttons.amend(display <-- showButtons.signal.map(if (_) "block" else "none"))
//         )
//     }
// }
