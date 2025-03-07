package model

import model.json.Reader


/**
  * Case class used to represent tauri's debug tree automatically using upickle.
  *
  * @param input the input string the tree node has to parse
  * @param root the debug tree root node
  * @param isDebuggable if this tree is using live debugging operations like breakpoint skipping
  * @param refs A list of pairs: `Address` and `Reference Value` from `Parsley`'s State
  */
case class DebugTree(input: String, root: DebugNode, parserInfo: Map[String, List[(Int, Int)]], isDebuggable: Boolean, refs: Seq[(Int, String)] = Nil) derives Reader.upickle


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
  * @param isIterative if a node is iterative (and opaque)
  */
case class DebugNode(nodeId: Int, name: String, internal: String, success: Boolean,
        childId: Int, input: String, isLeaf: Boolean, isIterative: Boolean, newlyGenerated: Boolean) derives Reader.upickle {
  
    def isBreakpoint: Boolean = internal == "remoteBreak"
}

