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
        className := "debug-tree-container zoom-container",
        
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
        val hasUserType: Boolean = node.debugNode.internal != node.debugNode.name
        val compressed: Signal[Boolean] = node.children.signal.map(_.isEmpty)
        val treatIteratively: Var[Boolean] = Var(node.debugNode.isIterative)
        val hasOneChild: Signal[Boolean] = node.children.signal.map(_.length == 1)

        val iterativeNodeIndex: Var[Int] = Var(0)
        val moveIndex: Observer[Int] = iterativeNodeIndex.updater((currIndex, delta) => {
            val indexSum: Int = currIndex + delta
            val childrenLen: Int = node.children.now().length

            /* Wrap indices of children around */
            if indexSum < 0 then
                childrenLen - 1
            else if indexSum > childrenLen - 1 then
                0
            else
                indexSum
        })

        /* Vars to keep track of when the mouse is within either arrow button */
        val leftIterativeButtonHovered = Var(false)
        val rightIterativeButtonHovered = Var(false)

        /* Signal for when to show arrow buttons */
        val showIterativeButtons: Signal[Boolean] = compressed.not.combineWith(hasOneChild.not).map(_ && _ && node.debugNode.isIterative)

        div(
            className := "debug-node-container",

            /* Render a box for user-defined parser types */
            cls("type-box") := hasUserType,
            when (hasUserType) { 
                p(className := "type-box-name", node.debugNode.name)
            },

                
            /* Line connecting node to parent */
            div(className := "debug-node-line"),
            div(
                display.flex,
                flexDirection.row,
                alignItems.stretch,

                child(button(
                    className := "debug-node-iterative-buttons",
                    i(
                        cls("bi bi-caret-left-fill") <-- leftIterativeButtonHovered.signal,
                        cls("bi bi-caret-left") <-- leftIterativeButtonHovered.signal.not,
                        onMouseOver.mapTo(true) --> leftIterativeButtonHovered,
                        onMouseOut.mapTo(false) --> leftIterativeButtonHovered
                    ),
                    onClick.mapTo(-1) --> moveIndex,
                    onMouseOver.mapTo(true) --> leftIterativeButtonHovered,
                    onMouseOut.mapTo(false) --> leftIterativeButtonHovered

                )) <-- showIterativeButtons,

                div(
                    className := "debug-node",
                    flexGrow := 1,
                    /* Set reactive class names */
                    cls("compress") <-- compressed,
                    cls("fail") := !node.debugNode.success,
                    cls("leaf") := node.debugNode.isLeaf,
                    cls("iterative") := node.debugNode.isIterative,

                    /* Render debug node information */
                    div(
                        p(className := "debug-node-name", node.debugNode.internal),
                        child(p("Child #", text <-- iterativeNodeIndex.signal)) <-- treatIteratively.signal.combineWith(compressed.not).map(_ && _),
                        p(fontStyle := "italic", node.debugNode.input)
                    ),

                    /* Expand/compress node on click */
                    onClick(_
                        .sample(compressed)
                        .filter(_ => !node.debugNode.isLeaf)
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

                child(button(
                    className := "debug-node-iterative-buttons",
                    i(
                        cls("bi bi-caret-right-fill") <-- rightIterativeButtonHovered.signal,
                        cls("bi bi-caret-right") <-- rightIterativeButtonHovered.signal.not,
                        onMouseOver.mapTo(true) --> rightIterativeButtonHovered,
                        onMouseOut.mapTo(false) --> rightIterativeButtonHovered
                    ),
                    onClick.mapTo(1) --> moveIndex,
                    onMouseOver.mapTo(true) --> rightIterativeButtonHovered,
                    onMouseOut.mapTo(false) --> rightIterativeButtonHovered
                )) <-- showIterativeButtons,
                

            ),


            /* Set isLeaf/isCompressed indicators below node */
            if (node.debugNode.isLeaf) {
                div(className := "leaf-line")
            } else {
                child(p(className := "compress-ellipsis", "...")) <-- compressed
            },
            /* Flex container for rendering children */
            div(
                className := "debug-node-children-container",
                children <-- node.children.signal.combineWith(iterativeNodeIndex).map((nodes, index) => 
                    if node.debugNode.isIterative && !nodes.isEmpty then
                        List(ReactiveNodeDisplay(ReactiveNode(nodes(index))))
                    else
                        nodes.map(ReactiveNode.apply andThen ReactiveNodeDisplay.apply))
            )

        )
    }
}
