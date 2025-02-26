package controller.viewControllers

import com.raquo.laminar.api.L.*

/**
  * Object containing methods for manipulating the DebugTree.
  */
object DebugViewController {

    private val animationTimeSeconds: Float = 1.8

    private def animationString(isDebugSession: Boolean): String = {
        if (isDebugSession) {
            f"highlightDebugSession ${animationTimeSeconds}s infinite alternate ease-in"
        } else {
            ""
        }
    }
    
    val setBorderAnimation: Signal[String] = TreeViewController.isDebuggingSession.signal.map(animationString)
}
