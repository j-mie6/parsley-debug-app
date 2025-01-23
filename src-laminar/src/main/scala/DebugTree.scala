package Display
import upickle.default.{ReadWriter => RW, macroRW}

case class DebugTree(name: String, internal: String, success: Boolean,
     input: String, number: Int, children: List[DebugTree]) derives RW