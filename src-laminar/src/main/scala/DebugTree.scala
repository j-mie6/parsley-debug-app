package Display
import upickle.default.ReadWriter

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
case class DebugTree(name: String, internal: String, success: Boolean,
     input: String, number: Int, children: List[DebugTree]) derives ReadWriter