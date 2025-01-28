package debugger
import upickle.default.ReadWriter

/**
  * Case class used to represent tauri's debug tree automatically using upickle.
  *
  * @param name user defined name of the debug tree node
  * @param internal internal name of the debug tree node
  * @param success whether the input was successfully consumed
  * @param child_id an unique identifier number for the tree node
  * @param input the input string the tree node has to parse
  * @param children list of children nodes
  */
case class DebugNode(name: String, internal: String, success: Boolean,
    childId: Int, input: String, children: List[DebugNode]) derives ReadWriter