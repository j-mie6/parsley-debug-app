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
  * @param doesNeedBubbling if a node needs bubbling up to the first opaque parent (it is iterative an transparent)
  */
case class DebugNode(nodeId: Int, name: String, internal: String, success: Boolean,
    childId: Int, input: String, isLeaf: Boolean, doesNeedBubbling: Boolean) derives ReadWriter
