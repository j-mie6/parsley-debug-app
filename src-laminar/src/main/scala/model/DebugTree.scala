package model

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
  * @param nodeId an unique identifier number for the tree node
  * @param name user defined name of the debug tree node
  * @param internal internal name of the debug tree node
  * @param success whether the input was successfully consumed
  * @param childId an unique identifier number for the tree node
  * @param input the input string the tree node has to parse
  * @param children list of children nodes
  * @param isLeaf if a node is a leaf
  */
case class DebugNode(nodeId: Int, name: String, internal: String, success: Boolean,
    childId: Int, input: String, children: List[DebugNode], isLeaf: Boolean) derives ReadWriter

object DebugTree {
    lazy final val Sample: DebugTree = DebugTree(
        "hi world!",
        DebugNode(0, "~>", "~>", true, 0, "hi world!", List[DebugNode](
            DebugNode(1, "~>", "~>", true, 1, "hi", List[DebugNode](
                DebugNode(2, "'h'", "'h'", true, 1, "h", List[DebugNode](), true),
                DebugNode(3, "|", "|", true, 2, "i", List[DebugNode](
                    DebugNode(4, "\"ello\"", "\"ello\"", false, 0, "", List[DebugNode](), true),
                    DebugNode(5, "\"i\"", "\"i\"", true, 1, "i", List[DebugNode](), true)
                ), false)
            ), false),
            DebugNode(6, "\" world!\"", "\" world!\"", true, 2, "world!", List[DebugNode](), true)
        ), false)
    )
}
