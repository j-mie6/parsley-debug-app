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
  * @param input the input string the tree node has to parse
  * @param number an unique identifier number for the tree node
  * @param children list of children nodes
  */
case class DebugNode(name: String, internal: String, success: Boolean,
    input: String, number: Int, children: List[DebugNode]) derives ReadWriter


lazy final val _debug_tree_sample: DebugTree = DebugTree(
        "hi world!",
        DebugNode("~>", "~>", true, "hi world!", 0, List[DebugNode](
            DebugNode("~>", "~>", true, "hi world!", 1, List[DebugNode](
                DebugNode("'h'", "'h'", true, "hi world!", 1, List[DebugNode]()),
                DebugNode("|", "|", true, "hi world!", 2, List[DebugNode](
                    DebugNode("\"ello\"", "\"ello\"", false, "hi world!", 0, List[DebugNode]()),
                    DebugNode("\"i\"", "\"i\"", true, "hi world!", 1, List[DebugNode]())
                ))
            )),
            DebugNode("\" world!\"", "\" world!\"", true, "hi world!", 2, List[DebugNode]())
        ))
    )