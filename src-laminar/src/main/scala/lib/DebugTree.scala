package lib
import upickle.default.ReadWriter

/**
  * Case class used to represent tauri's debug tree automatically using upickle.
  *
  * @param input the input string the tree node has to parse
  * @param root the debug tree root node
  */
case class DebugTree(input: String, root: DebugNode) derives ReadWriter

/**
  * Case class used to represent tauri's debug tree automatically using upickle.
  *
  * @param name user defined name of the debug tree node
  * @param internal internal name of the debug tree node
  * @param success whether the input was successfully consumed
  * @param childId an unique identifier number for the tree node
  * @param input the input string the tree node has to parse
  * @param children list of children nodes
  */
case class DebugNode(name: String, internal: String, success: Boolean,
    childId: Int, input: String, children: List[DebugNode]) derives ReadWriter

object DebugTree {
    lazy final val Sample: DebugTree = DebugTree(
        "hi world!",
        DebugNode("~>", "~>", true, 0, "hi world!", List[DebugNode](
            DebugNode("~>", "~>", true, 1, "hi", List[DebugNode](
                DebugNode("'h'", "'h'", true, 1, "h", List[DebugNode]()),
                DebugNode("|", "|", true, 2, "i", List[DebugNode](
                    DebugNode("\"ello\"", "\"ello\"", false, 0, "", List[DebugNode]()),
                    DebugNode("\"i\"", "\"i\"", true, 1, "i", List[DebugNode]())
                ))
            )),
            DebugNode("\" world!\"", "\" world!\"", true, 2, "world!", List[DebugNode]())
        ))
    )
}
