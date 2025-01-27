package debugger
import upickle.default.ReadWriter

/**
  * Case class used to represent tauri's debug tree automatically using upickle.
  *
  * @param input the input string the tree node has to parse
  * @param root the debug tree root node
  */
case class DebugTree(input: String, root: DebugNode) derives ReadWriter

object DebugTree {
    lazy final val Sample: DebugTree = DebugTree(
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
}