package model

import scala.util.Try
import scala.util.Failure
import scala.util.Success

import upickle.default as up


/* Error returned by Serializer/Deserializer */
case class JsonError(private val msg: String)


/* Defines JSON parsing interface */
trait Deserialize[O] {
    def read(s: String): Either[JsonError, O]
}

object Deserialize {
    /* Expose deserialize object for calling read function */
    def apply[O](using deserialize: Deserialize[O]) = deserialize 
    
    type upickle[T] = up.Reader[T]

    /* Delegate to upickle for JSON reading */
    given upReader: [O: up.Reader] => Deserialize[O] {
        def read(s: String) = {
            Try(up.read[O](s).nn) match 
                case Failure(err) => Left(JsonError(err.getMessage))
                case Success(value) => Right(value)
        }
    }
    
}


/* Defines JSON writing interface */
trait Serialize[JsonError, I] {
    def write(input: I): Either[JsonError, String]
}

object Serialize {
    /* Expose serialize object for calling write function */
    def apply[I](using serialize: Serialize[JsonError, I]) = serialize 
    
    type upickle[T] = up.Writer[T]

    /* Delegate to upickle for JSON writing */
    given upWriter: [I: up.Writer] => Serialize[JsonError, I] {
        def write(input: I) = {
            Try(up.write[I](input).nn) match
                case Failure(err) => Left(JsonError(err.getMessage))
                case Success(value) => Right(value)
        }
    }

}
