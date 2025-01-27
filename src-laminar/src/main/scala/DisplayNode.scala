package displays
import com.raquo.laminar.api.L.*

import debugger.DebugNode

/** Renderer for single DebugNode
  * @param debugNode case class for holding debug values to be rendered
  */
class DisplayNode(debugNode: DebugNode) {
    /* Element rendering node's values */
    private lazy val node: Element = div(
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
    )

    /* Flexbox to hold children of debugNode */
    private lazy val children: Element = div(
        display := "flex",
        flexDirection := "row",
        flexWrap := "nowrap",
        flexBasis := "auto",

        justifyContent := "space-evenly",
        alignItems := "space-evenly",

        debugNode.children.map((child) => DisplayNode(child).element)
    )

    /* Element for rendering as HTML */
    lazy val element: Element = div(
        node,
        children
    )
    
}


object DisplayNode {

}