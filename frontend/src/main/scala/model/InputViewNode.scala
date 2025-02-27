package model

/**
 * Represents a 'child' node's fragment of parsed text. These are only available on
 * named nodes to avoid cluttering and fragmentation down to individual characters.
 */
case class InputViewNode(source: String, from: Int, to: Int)