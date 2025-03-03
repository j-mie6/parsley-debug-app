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
    val zoomUpdate: Observer[Double] = zoomFactor.updater[Double]((prev, delta) => 
        val zoomChange: Double = 1.0 + (delta * zoomSpeed)
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

        /* If the current node should be considered iterative */
        val treatIteratively: Var[Boolean] = Var(node.debugNode.isIterative)

        /* In the case of an iterative node, should all children be expanded */
        val expandAllChildren: Var[Boolean] = Var(!node.debugNode.isIterative)

        /* If the current node's children are visible (or exist) */
        val compressed: Signal[Boolean] = node.children.signal.map(_.isEmpty)

        /* If the current node only has one child */
        val hasOneChild: Signal[Boolean] = node.children.signal.map(_.length == 1)

        /* The index of the current child rendered for iterative nodes */
        val iterativeNodeIndex: Var[Int] = Var(0)

        /* Incrementing iterative node index */
        val moveIndex: Observer[Int] = iterativeNodeIndex.updater((currIndex, delta) => {
            /* Wrap indices of children around */
            val childrenLen: Int = node.children.now().length
            val indexSum: Int = (currIndex + delta) % childrenLen
            
            if (indexSum < 0) then indexSum + childrenLen else indexSum
        })

        /* Signal for when to show arrow buttons */
        val showIterativeOneByOne: Signal[Boolean] = compressed.not.combineWithFn(hasOneChild.not, expandAllChildren.signal.not)(_ && _ && _ && node.debugNode.isIterative)

        /* Signal for if a node has more than 10 children */
        val moreThanTenChildren: Signal[Boolean] = node.children.signal.map(_.length >= 10)

        def iterativeArrowButton(isRight: Boolean): HtmlElement = {
            // val singleDirection: String = if isRight then "end" else "start"

            val singleHoverVar: Var[Boolean] = Var(false)
            button(                    
                    className := "debug-node-iterative-buttons",
                    marginBottom.px := 2, 
                    i(
                        styleAttr := (if !isRight then "transform: scaleX(-1)" else ""),
                        cls(s"bi bi-play-fill") <-- singleHoverVar.signal,
                        cls(s"bi bi-play") <-- singleHoverVar.signal.not,
                        onMouseOver.mapTo(true) --> singleHoverVar,
                        onMouseOut.mapTo(false) --> singleHoverVar,
                        height.px := 16,
                        margin.auto,
                    ),
                    onClick.mapTo(if isRight then 1 else -1) --> moveIndex,
                    onMouseOver.mapTo(true) --> singleHoverVar,
                    onMouseOut.mapTo(false) --> singleHoverVar
            )
        }

        def iterativeDoubleArrowButton(isRight: Boolean): HtmlElement = {
            // val doubleDirection: String = if isRight then "forward" else "backward"
            val doubleHoverVar: Var[Boolean] = Var(false)

            button(
                    className := "debug-node-iterative-buttons",
                    i(
                        styleAttr := (if !isRight then "transform: scaleX(-1)" else ""),
                        cls(s"bi bi-fast-forward-fill") <-- doubleHoverVar.signal,
                        cls(s"bi bi-fast-forward") <-- doubleHoverVar.signal.not,
                        onMouseOver.mapTo(true) --> doubleHoverVar,
                        onMouseOut.mapTo(false) --> doubleHoverVar,
                        height.px := 16,
                        margin.auto,
                    ),
                    onClick.mapTo(if isRight then 5 else -5) --> moveIndex,
                    onMouseOver.mapTo(true) --> doubleHoverVar,
                    onMouseOut.mapTo(false) --> doubleHoverVar
            )                    
        }

        def iterativeChildrenPercentage: Signal[Double] = iterativeNodeIndex.signal.combineWith(node.children.signal).map { (index, ns) =>
            ((index.toDouble + 1.0) / (ns.length)) * 100
        }

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

                div (
                    display.flex,
                    flexDirection.column,
                    alignContent.end,
                    justifyContent.center,

                    child(iterativeArrowButton(isRight = false)) <-- showIterativeOneByOne,
                    child(iterativeDoubleArrowButton(isRight = false)) <-- showIterativeOneByOne.combineWithFn(moreThanTenChildren)(_ && _),

                ),


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
                        p(fontStyle := "italic", node.debugNode.input),
                        child(p(fontSize.px := 10, marginBottom.px := -5, marginTop.px := 5, "Child ", text <-- iterativeNodeIndex.signal)) <-- showIterativeOneByOne,
                        child(
                            div(cls("iterative-progress-container"),
                                div(
                                cls("iterative-progress-fill"),
                                width <-- iterativeChildrenPercentage.map(p => s"${p}%")
                                )
                            )
                        ) <-- showIterativeOneByOne
                    ),

                    /* Expand/compress node with (with arrows for iterative) on click */
                    onClick(_
                        .sample(compressed)
                        .filter(_ => !node.debugNode.isLeaf)
                        .flatMapMerge(
                            if (_) { 
                                /* If no children, load them in */
                                Tauri.invoke(Command.FetchNodeChildren, node.debugNode.nodeId).collectRight
                            } else {
                                /* Otherwise set them to empty list */
                                expandAllChildren.set(false)
                                EventStream.fromValue(Nil)
                            }
                        )
                    ) --> node.children.writer,

                    /* Expand/compress iterative nodes to all children on double click */
                    onDblClick(_
                        .filter(_ => node.debugNode.isIterative)
                        .sample(compressed)
                        .flatMapMerge(EventStream.fromValue(_))
                    ) --> expandAllChildren

                ),
                
                div(
                    display.flex,
                    flexDirection.column,
                    alignItems.flexStart,
                    justifyContent.center,
                    child(iterativeArrowButton(isRight = true)) <-- showIterativeOneByOne,
                    child(iterativeDoubleArrowButton(isRight = true)) <-- showIterativeOneByOne.combineWithFn(moreThanTenChildren)(_ && _),
                ),
                

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
                children <-- node.children.signal.combineWith(iterativeNodeIndex, compressed.not, expandAllChildren)
                    .map((nodes, index, notCompressed, expandAllCs) => 
                        if node.debugNode.isIterative && notCompressed && !expandAllCs then
                            List(ReactiveNodeDisplay(ReactiveNode(nodes(index))))
                        else
                            nodes.map(ReactiveNode.apply andThen ReactiveNodeDisplay.apply)),
            )
        )
    }
}
