package model

import scala.util.Try

import upickle.default as up


/* Defines JSON parsing interface */
trait Deserialize[E <: DillError, O] {
    def read(s: String, handleError: (Throwable => E)): Either[E, O]
}

object Deserialize {
    def apply[E <: DillError, O](using deserialize: Deserialize[E, O]) = deserialize 
    
    /* Delegate to upickle for JSON reading */
    given upReader: [E <: DillError, O] => Deserialize[E, O] {
        def read(s: String, handleError: (Throwable => E)) = {
            given up.Reader[O] = ???
            Try(up.read[O](s).nn)
                .toEither
                .swap
                .map(handleError)
                .swap
        }
    }
    
}


/* Defines JSON writing interface */
trait Serialize[E <: DillError, I] {
    def write(input: I, handleError: (Throwable => E)): Either[E, String]
}

object Serialize {
    def apply[E <: DillError, I](using serialize: Serialize[E, I]) = serialize 
    
    /* Delegate to upickle for JSON writing */
    given upWriter: [E <: DillError, I] => Serialize[E, I] {
        def write(input: I, handleError: (Throwable => E)) = {
            given up.Writer[I] = ???
            Try(up.write[I](input).nn)
                .toEither 
                .swap
                .map(handleError)
                .swap : Either[E, String]
        }
    }

}



