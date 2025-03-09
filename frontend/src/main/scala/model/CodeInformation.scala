package model

import model.json.Reader

/**
  * Represents information about the user written parser code.
  *
  * @param info Map from filename 
  */
final case class CodeFileInformation(info: Map[String, List[(Int, Int)]]) derives Reader.upickle
