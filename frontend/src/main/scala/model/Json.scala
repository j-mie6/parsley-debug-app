package model

import scala.util.{Try, Failure, Success}
import scala.deriving.Mirror
import scala.reflect.ClassTag

import upickle.default as up


/* Error returned by Serializer/Deserializer */
case class JsonError(private val msg: String)


/* Defines JSON parsing interface */
trait Reader[O] {
    def read(s: String): Either[JsonError, O]
}

object Reader {
    /* Expose deserialize object for calling read function */
    def apply[O](using deserialize: Reader[O]) = deserialize 
    
    /* Define upickle reader */
    type upickle[O] = up.Reader[O]
    object upickle {
        inline def derived[O](using Mirror.Of[O]) = up.Reader.derived[O]
    }

    /* Delegate to upickle for JSON reading */
    given upickleReader: [O: upickle] => Reader[O] {
        def read(s: String) = {
            Try(up.read[O](s).nn) match 
                case Failure(err) => Left(JsonError(err.getMessage))
                case Success(value) => Right(value)
        }
    }
    
}


/* Defines JSON writing interface */
trait Writer[I] {
    def write(input: I): Either[JsonError, String]
}

object Writer {
    /* Expose serialize object for calling write function */
    def apply[I](using serialize: Writer[I]) = serialize 

    /* Define upickle writer */
    type upickle[T] = up.Writer[T] 
    object upickle {
        inline def derived[T](using Mirror.Of[T], ClassTag[T]) = up.Writer.derived[T]
    }
    
    /* Delegate to upickle for JSON writing */
    given upickleWriter: [I: upickle] => Writer[I] {
        def write(input: I) = {
            Try(up.write[I](input).nn) match
                case Failure(err) => Left(JsonError(err.getMessage))
                case Success(value) => Right(value)
        }
    }

}
