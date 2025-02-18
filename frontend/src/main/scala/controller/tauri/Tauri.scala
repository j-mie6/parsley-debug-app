package controller.tauri

import scala.util.Try
import scala.concurrent.Future

import com.raquo.laminar.api.L.*


/**
  * Object containing methods for communicating with the Tauri backend.
  */
object Tauri {

    /** 
     * Invoke specified backend Tauri command.
     *
     * @param cmd Name of Tauri command to invoke.
     * @return EventStream holding the result of the command call
     */
    def invoke(cmd: Command): EventStream[Try[cmd.Response]] = cmd.invoke()
    
    /**
     * Invoke specified backend Tauri command with arguments.
     *
     * @param cmd Name of Tauri command to invoke
     * @param args Map of argument name to argument value. Argument names should be CamelCase.
     * @return EventStream holding the result of the command call
     */
    def invoke(cmd: Command, args: Map[String, Any]): EventStream[Try[cmd.Response]] = cmd.invoke(args)


    /**
     * Listen to a backend Tauri event.
     *
     * @param event Name of Tauri Event to listen for.
     * @return Tuple of EventStream and Future holding a function to un-listen
     */
    def listen(event: Event): (EventStream[Try[event.Response]], Future[() => Unit]) = event.listen()

}