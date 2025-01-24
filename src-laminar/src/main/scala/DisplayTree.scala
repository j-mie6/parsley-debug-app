package displays

import com.raquo.laminar.api.L.*

import debugger.DebugNode

/**
  * DisplayTree creates the HTML element to display a DebugTree
  *
  * @param placeholderText placeholder comment at the top of file
  * @param children list of children of display tree's root node
  */
class DisplayTree(val node: Element, val children: List[DisplayTree]) {

    def nodeElement(depth: Int = 1): Element = div(
        padding := s"${2.5 - depth * 0.35}em",
        paddingLeft := "1em",
        paddingRight := "1em",
        
        border := "1px solid #2E2F30",
        borderRadius.px := 20,

        color := "#2E2F30",
        backgroundColor := s"hsl(158.3, 52.2%, ${60 + depth * 8}%)",

        textAlign := "center",
        fontSize := s"${14 - depth}pt",

        // placeholderText
        node
    )

    /* HTML element to display DisplayTree */
    def element(depth: Int = 1): Element = {
        div(
            margin := "0.6em",
            
            nodeElement(depth),      
                  
            div(
                display := "flex",
                flexDirection := "row",
                flexWrap := "nowrap",

                justifyContent := "center",

                children.map((child) => 
                    child.element(depth + 1)
                )
            )
        )
    }
}


object DisplayTree {
//     // Example of a DisplayTree to use in testing
//     final val SampleTree: DisplayTree = {
//         DisplayTree(p("Carmine"), List(
//             DisplayTree(p(text = "August"), List(
//                 DisplayTree(p("Marc")),
//                 DisplayTree(p("Christoper"), List(
//                     DisplayTree(p("Bailey")), 
//                     DisplayTree(p("Dexter"))
//                 )),
//                 DisplayTree(p("Nicolas"), List(DisplayTree(p("Kal")))
//             )),
//             DisplayTree(p("Francis"), List(
//                 DisplayTree(p("Gian-Carlo"), List(DisplayTree(p("Gian-Carla")))),   
//                 DisplayTree(p("Roman")),
//                 DisplayTree(p("Sofia"), List(
//                     DisplayTree(p("Romy")),
//                     DisplayTree(p("Cosima"))
//                 ))
//             )),
//             DisplayTree(p("Talia"), List(
//                 DisplayTree(p("Jason"), List(
//                     DisplayTree(p("Marlowe")),
//                     DisplayTree(p("Una"))
//                 )),
//                 DisplayTree(p("Robert")),
//             ))
//         )))
//    }
    
    // Secondary constructors
    def apply(node: Element): DisplayTree = DisplayTree(node, List[DisplayTree]())
    def apply(node: Element, children: List[DisplayTree]): DisplayTree = new DisplayTree(node, children)

    private def debugNodeElement(tree: DebugNode): Element = {
        p(tree.name)
    }
    
    def from(tree: DebugNode): DisplayTree = {
        DisplayTree(
            DisplayTree.debugNodeElement(tree), 
            tree.children.map(child => DisplayTree.from(child))
        )
    }

    
}