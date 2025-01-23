package Display

import com.raquo.laminar.api.L.*


/**
  * DisplayTree creates the HTML element to display a DebugTree
  *
  * @param placeholderText placeholder comment at the top of file
  * @param children list of children of display tree's root node
  */
class DisplayTree(val placeholderText: String, val children: List[DisplayTree]) {
    
    def node(depth: Int = 1): Element = div(
        paddingTop := "1em",
        paddingBottom := "2em",
        
        color := "#2E2F30",
        
        textAlign := "center",

        s"${depth}: $placeholderText"
    )

    /* HTML element to display DisplayTree */
    def element(depth: Int = 1): Element = {
        div(
            padding := "1em",
            
            margin := "0.2em",
            marginBottom := "0.1em",
            marginTop := "0.25em",
            
            // border := "2px solid #ffffff",
            borderRadius.px := 20,

            color := "#2E2F30",
            backgroundColor := s"hsl(158.3, 52.2%, ${72.9 + depth * 5}%)",
            
            node(depth),
            
            div(
                display := "flex",
                flexDirection := "row",
                flexWrap := "nowrap",

                // justifyContent := "space-between",
                justifyContent := "center",

                children.map((child) => 
                    child.element(depth + 1)
                )
            )
        )
    }
}


object DisplayTree {
    // Example of a DisplayTree to use in testing
    final val SAMPLE_TREE: DisplayTree = {
        DisplayTree("Carmine", List(
            DisplayTree("August", List(
                DisplayTree("Marc"),
                DisplayTree("Christoper", List(
                    DisplayTree("Bailey"), 
                    DisplayTree("Dexter")
                )),
                DisplayTree("Nicolas", List(DisplayTree("Kal")))
            )),
            DisplayTree("Francis", List(
                DisplayTree("Gian-Carlo", List(DisplayTree("Gian-Carla"))),   
                DisplayTree("Roman"),
                DisplayTree("Sofia", List(
                    DisplayTree("Romy"),
                    DisplayTree("Cosima")
                ))
            )),
            DisplayTree("Talia", List(
                DisplayTree("Jason", List(
                    DisplayTree("Marlowe"),
                    DisplayTree("Una")
                )),
                DisplayTree("Robert"),
            ))
        ))
    }
    
    // Secondary constructors
    def apply(placeholderText: String, children: List[DisplayTree]): DisplayTree = new DisplayTree(placeholderText, children)
    def apply(placeholderText: String): DisplayTree = DisplayTree(placeholderText, List[DisplayTree]())
}