package view

import com.raquo.laminar.api.L.*

import model.{DebugTree, DebugNode, ReactiveNode}
import controller.viewControllers.MainViewController
import controller.viewControllers.TabViewController
import controller.tauri.{Tauri, Command}


/**
* Object containing rendering methods for the Debug Tree Display, which is
* used to display the debug tree in the UI.
*/
object DebugTreeDisplay {
    
    /* Variable that keeps track of how much the tree has been zoomed into */
    val zoomFactor: Var[Double] = Var(defaultZoom)
    
    val zoomSpeed: Float = -0.002   /* Speed of zooming in and out */
    val defaultZoom: Double = 1.0   /* Default zoom factor */
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

    /* Resets the zoomFactor to 1 */
    val resetZoom = Observer(_ => zoomFactor.set(defaultZoom))


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

        /* Incrementing iterative node index with wrapping and early stopping when hitting the first and last nodes */
        val moveIndex: Observer[Int] = iterativeNodeIndex.updater((currIndex, delta) => {
            val childrenLen = node.children.now().length

            if (childrenLen == 0) then
                currIndex
            else
                val absDelta = math.abs(delta)
                
                if (absDelta == 5) then
                    val isForward = delta > 0
                    val nearLast = currIndex + 5 >= childrenLen
                    val nearFirst = currIndex - 5 < 0

                    if (isForward) then
                        if (nearLast) then
                            if (currIndex != childrenLen - 1) then childrenLen - 1 else (currIndex + 5) % childrenLen
                        else currIndex + 5
                    else
                        if (nearFirst) then
                            if (currIndex != 0) then 0 else (currIndex - 5 + childrenLen) % childrenLen
                        else currIndex - 5
                else
                    /* Normal increment or decrement with wrapping */
                    val newIndex = (currIndex + delta) % childrenLen
                    if (newIndex < 0) then newIndex + childrenLen else newIndex
        })



        /* Signal for when to show arrow buttons */
        val showIterativeOneByOne: Signal[Boolean] = compressed.not.combineWithFn(hasOneChild.not, expandAllChildren.signal.not)(_ && _ && _ && node.debugNode.isIterative)

        /* Signal for if a node has more than 10 children */
        val moreThanTenChildren: Signal[Boolean] = node.children.signal.map(_.length >= 10)

        /* Button to increment selected iterative child */
        def iterativeArrowButton(icon: String, increment: Int, isRight: Boolean): HtmlElement = {
            val hoverVar: Var[Boolean] = Var(false)
            
            button(
                className := "debug-node-iterative-buttons",
                marginBottom.px := 2,

                i(
                    cls(s"bi bi-$icon") <-- hoverVar.signal.not,
                    cls(s"bi bi-$icon-fill") <-- hoverVar.signal,
                                        
                    height.px := 16,
                    margin.auto,

                    whenNot (isRight) {
                        transform := "scaleX(-1)"
                    },
                    
                    onMouseOver.mapTo(true) --> hoverVar,
                    onMouseOut.mapTo(false) --> hoverVar,
                ),

                onClick.mapTo(if isRight then increment else -increment) --> moveIndex,
                
                onMouseOver.mapTo(true) --> hoverVar,
                onMouseOut.mapTo(false) --> hoverVar
            )
        }

        def iterativeChildrenPercentage: Signal[Double] = iterativeNodeIndex.signal
            .combineWith(node.children.signal)
            .map((index, ns) => ((index.toDouble + 1.0) / ns.length) * 100)

        def iterativeProgress = {
            div(
                className := "iterative-progress-container",

                children <-- iterativeNodeIndex.signal
                    .combineWith(node.children.signal.map(0 until _.length))
                    .map((index, range) => range.map(i => 
                        div(
                            className := "iterative-progress", 
                            ".",
                            cls("fill") := i <= index,
                        )
                    )),
            )
        }

        def singleArrow(isRight: Boolean) = iterativeArrowButton(icon = "play", 1, isRight)
        def doubleArrow(isRight: Boolean) = iterativeArrowButton(icon = "fast-forward", 5, isRight)

        def arrows(isRight: Boolean) = {
            div(
                display.flex,
                flexDirection.column,
                alignItems.flexStart,
                justifyContent.center,
                
                child(singleArrow(isRight)) <-- showIterativeOneByOne,
                child(doubleArrow(isRight)) <-- showIterativeOneByOne
                    .combineWithFn(moreThanTenChildren)(_ && _),
            )
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

                arrows(isRight = false),

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
                        child(p(fontSize.px := 10, marginBottom.px := -5, marginTop.px := 5, "child ", text <-- iterativeNodeIndex.signal)) <-- showIterativeOneByOne,
                        child(iterativeProgress) <-- showIterativeOneByOne,
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
                
                arrows(isRight = true),
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
