package model


case class DillError(private val msg: String)

object DillError {
    def apply(throwable: Throwable) = new DillError(throwable.getMessage)
}
