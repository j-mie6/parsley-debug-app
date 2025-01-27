package displays

import com.raquo.laminar.api.L.*

import lib.DebugTree
import lib.DebugNode

/**
  * DisplayTree creates the HTML element to display a DebugTree
  *
  * @param debugTree a DebugTree parsed from JSON
  */
object DebugTreeDisplay {
    def apply(tree: DebugTree): HtmlElement = {
        /* HTML element exposed to be rendered */
        div(
            display.flex,
            flexDirection.column,
            flexWrap.nowrap,
            flexBasis.auto,
            justifyContent := "flex-start",
            alignItems := "center",
    
            h1(
                textAlign := "center", 
                tree.input
            ),
            div(
                DebugNodeDisplay(tree.root)
            )
        )
    }
}


// object DisplayTree {
//     // Example DisplayTree to use in testing
//     final val Sample: DisplayTree = DisplayTree(DebugTree.Sample)
// }


/** Renderer for single DebugNode
  * @param debugNode case class for holding debug values to be rendered
  */
object DebugNodeDisplay {
    def apply(debugNode: DebugNode): HtmlElement = {
        /* Element for rendering as HTML */
        div(
            div(
                className := s"${if debugNode.success then "success" else "fail"}-node",
                
                padding.rem := 0.5,
                paddingBottom.rem := 0.2,
                paddingTop.rem := 0.5,
                
                margin.px := 2,
                
                borderWidth.px := 2,
                borderRadius.px := 10,
                borderColor := "#96DEC4",
                borderStyle := {if debugNode.success then "solid" else "dashed"},
        
                color := {if debugNode.success then "#2E2F30" else "#96DEC4"},
                backgroundColor := s"rgba(150, 222, 196, ${if debugNode.success then 0.6 else 0.05})",
        
                textAlign := "center",
                width := "auto",
                
                div(
                    p(fontWeight := "bold", debugNode.name), 
                    p(fontStyle := "italic", debugNode.input)
                )
            ),
            div(
                display := "flex",
                flexDirection := "row",
                flexWrap := "nowrap",
                flexBasis := "auto",
        
                justifyContent := "space-evenly",
                alignItems := "space-evenly",
        
                debugNode.children.map((child) => DebugNodeDisplay(child))
            )
        )
    }
}


// object DisplayNode {

// }