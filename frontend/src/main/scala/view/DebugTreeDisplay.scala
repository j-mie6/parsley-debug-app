package view

import com.raquo.laminar.api.L.*

import model.{DebugTree, DebugNode, ReactiveNode}
import controller.viewControllers.MainViewController
import controller.viewControllers.SettingsViewController
import controller.viewControllers.StateManagementViewController
import controller.viewControllers.TabViewController
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

    /* Resets the zoomFactor to 1 */
    val resetZoom = Observer(_ => zoomFactor.set(1.0))


    /**
    * Render the entire tree from the root node downwards.
    *
    * @param tree The debug tree to render.
    * 
    * @return HTML element representing the whole tree.
    */
    def apply(tree: DebugTree): HtmlElement = 
        StateManagementViewController.setRefs(tree.refs)
        StateManagementViewController.setLocalRefs(tree.refs)

        div(
        className := "debug-tree-container zoom-container",
        
        styleAttr <-- zoomFactor.signal.map(factor => s"transform: scale($factor);"),
        wheelHandler,
        
        tree.refs.map(StateRef(_)),

        ReactiveNodeDisplay(ReactiveNode(tree.root)),
    )

}


/** Render a Ref passed as to a breakpoint */
private object StateRef {
    def apply(codedRef: (Int, String)): HtmlElement = {
        p(
            className := "debug-tree-ref",
            s"R${subscriptInt(codedRef._1)}: ${codedRef._2}"
        )
    }

    private def subscriptInt(x: Int): String = {
        val subscriptInts = "\u2080\u2081\u2082\u2083\u2084\u2085\u2086\u2087\u2088\u2089"

        x.toString
            .map(i => subscriptInts.charAt(i.asDigit))
            .mkString
    }
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

        /**
          * The various states of expansion for a reactive node
          */
        enum ExpansionState:
            case NoChildren      /* Compressed */
            case OneIterativeChild /* Iterative single-child */
            case AllChildren     /* Either non-iterative or iterative fully expanded */

        /* Tracker for the expansion state of the current reactive node */
        val expansionState: Signal[ExpansionState] = 
            compressed.combineWith(expandAllChildren, treatIteratively.signal).map {
                (isCompressed: Boolean, expandAll: Boolean, isIter: Boolean) =>
                    if (isCompressed) 
                        ExpansionState.NoChildren
                    else if (isIter && !expandAll) 
                        ExpansionState.OneIterativeChild
                    else 
                        ExpansionState.AllChildren
         }

        /**
         * An observer that updates the current node index (iterativeNodeIndex) when a delta is received.
         * If there are no children (childrenLen == 0), we simply return the current index.
         * If the incoming delta's absolute value matches the skipAmount (default 5),
         * we perform a skip forward or backward in larger steps, with checks to avoid
         * going out of range. If we're already near the last or first index, we clamp
         * the index to the boundary (or wrap if already at that boundary).
         * Otherwise, we do a normal increment or decrement (with wrapping) of the current index.
         */
        val moveIndex: Observer[Int] = iterativeNodeIndex.updater((currIndex, delta) => {
            /* Number of child nodes available */
            val childrenLen = node.children.now().length
            val skipAmount = SettingsViewController.getNumSkipIterativeChildren.now()

            def getNearWrap(wrapCondition: Boolean, clampValue: Int, wrapIncr: Int, notWrapValue: Int): Int = {
                if (wrapCondition) then
                    /* If we're not already on the clamp value, snap to it; otherwise, wrap */
                    if (currIndex != clampValue) then clampValue else (currIndex + wrapIncr) % childrenLen
                else
                    /* If not near the clampValue then move to notWrapValue */
                    notWrapValue
            }

            /* If there are children */
            if (childrenLen != 0) then
                /* Check how large the move is (absolute value) */
                val absDelta = math.abs(delta)
                
                /* If this delta is exactly the "skip amount" */
                if (absDelta == skipAmount) then
                    val isForward: Boolean = delta > 0
                    /* nearLast: if adding skipAmount goes beyond or right at the last index */
                    val nearLast: Boolean = currIndex + skipAmount >= childrenLen

                    /* nearFirst: if subtracting skipAmount goes below 0 */
                    val nearFirst: Boolean = currIndex - skipAmount < 0

                    if (isForward) then
                        /* Skip forward */
                        getNearWrap(nearLast, childrenLen - 1, skipAmount, currIndex + skipAmount)
                    else
                        /* Skip backward */
                        getNearWrap(nearFirst, 0, childrenLen - skipAmount, currIndex - skipAmount)
                else
                    /* If delta is not a large skip, do a normal +/- 1 move (wrapped) */
                    val newIndex = (currIndex + delta) % childrenLen
                    if (newIndex < 0) then newIndex + childrenLen else newIndex
            else
                /* If there are no children, no update; remain at currIndex */
                currIndex
        })

        /* Signal for when to show arrow buttons */
        val showIterativeOneByOne: Signal[Boolean] = compressed.not.combineWithFn(hasOneChild.not, expandAllChildren.signal.not)(_ && _ && _ && node.debugNode.isIterative)

        /* Signal for if a node has more than 10 children */
        val moreThanTenChildren: Signal[Boolean] = node.children.signal.map(_.length >= 10)

        /* Button to increment selected iterative child */
        def iterativeArrowButton(icon: String, increment: Signal[Int], isRight: Boolean): HtmlElement = {
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

                onClick(event => event.sample(increment).map(incr => if isRight then incr else -incr)) --> moveIndex,
                
                onMouseOver.mapTo(true) --> hoverVar,
                onMouseOut.mapTo(false) --> hoverVar
            )
        }

        /** Tracker for how many iterative children we have scrolled through */
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

        def singleArrow(isRight: Boolean) = iterativeArrowButton(icon = "play", Signal.fromValue(1), isRight)
        def doubleArrow(isRight: Boolean) = iterativeArrowButton(icon = "fast-forward", SettingsViewController.getNumSkipIterativeChildren.signal, isRight)

        def arrows(isRight: Boolean) = {
            div(
                className := "iterative-button-container",
                
                child(singleArrow(isRight)) <-- showIterativeOneByOne,
                child(doubleArrow(isRight)) <-- showIterativeOneByOne
                    .combineWithFn(moreThanTenChildren)(_ && _),
            )
        }


        div(
            className := "debug-node-container",

            /* Set reactive class names */
            cls("compress") <-- compressed,
            cls("fail") := !node.debugNode.success,
            cls("leaf") := node.debugNode.isLeaf,
            cls("iterative") := node.debugNode.isIterative,
            cls("debug") := node.debugNode.isBreakpoint,

            /* Render a box for user-defined parser types */
            cls("type-box") := hasUserType,
            when (hasUserType) { 
                p(className := "type-box-name", node.debugNode.name)
            },
                
            /* Line connecting node to parent */
            div(className := "debug-node-line"),

            div(
                cls("iterative-debug-node-container") := node.debugNode.isIterative,
                arrows(isRight = false),

                div(
                    className := "debug-node",
                    flexGrow := 1,

                    /* Render debug node information */
                    div(
                        p(className := "debug-node-name", node.debugNode.internal),
                        p(fontStyle := "italic", node.debugNode.input),
                        child(p(className := "debug-node-iterative-child-text", "child ", text <-- iterativeNodeIndex.signal)) <-- showIterativeOneByOne,
                        child(iterativeProgress) <-- showIterativeOneByOne,
                    ),

                    onClick(_
                        .filter(_ => !node.debugNode.isLeaf)
                        .sample(expansionState)
                        .flatMapMerge {
                            _ match
                                case ExpansionState.AllChildren => 
                                    EventStream.fromValue(Nil)
                                case ExpansionState.OneIterativeChild => 
                                    expandAllChildren.set(true)
                                    Tauri.invoke(Command.FetchNodeChildren, node.debugNode.nodeId).collectRight
                                case ExpansionState.NoChildren => 
                                    if (node.debugNode.isIterative) {
                                        expandAllChildren.set(false)
                                    }
                                    Tauri.invoke(Command.FetchNodeChildren, node.debugNode.nodeId).collectRight
                        }
                    ) --> node.children.writer
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
