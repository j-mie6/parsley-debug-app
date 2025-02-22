package model

import scala.util.Try

import upickle.default as up


/* Defines JSON parsing interface */
trait Deserialize[E <: DillError, O] {
    def read(s: String, handleError: (Throwable => E)): Either[E, O]
}

object Deserialize {
    /* Expose deserialize object for calling read function */
    def apply[E <: DillError, O](using deserialize: Deserialize[E, O]) = deserialize 
    
    type upickle[T] = up.Reader[T]

    /* Delegate to upickle for JSON reading */
    given upReader: [E <: DillError, O: up.Reader] => Deserialize[E, O] {
        def read(s: String, handleError: (Throwable => E)) = {
            Try(up.read[O](s).nn)
                .toEither
                .swap.map(handleError).swap
        }
    }
    
}


/* Defines JSON writing interface */
trait Serialize[E <: DillError, I] {
    def write(input: I, handleError: (Throwable => E)): Either[E, String]
}

object Serialize {
    /* Expose serialize object for calling write function */
    def apply[E <: DillError, I](using serialize: Serialize[E, I]) = serialize 
    
    type upickle[T] = up.Writer[T]

    /* Delegate to upickle for JSON writing */
    given upWriter: [E <: DillError, I: up.Writer] => Serialize[E, I] {
        def write(input: I, handleError: (Throwable => E)) = {
            Try(up.write[I](input).nn)
                .toEither 
                .swap.map(handleError).swap
        }
    }

}



