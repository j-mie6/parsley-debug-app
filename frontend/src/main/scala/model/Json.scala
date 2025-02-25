package model.json

import scala.util.{Try, Failure, Success}
import scala.deriving.Mirror
import scala.reflect.ClassTag

import upickle.default as up


/* Error returned by Serializer/Deserializer */
case class JsonError(private val msg: String)

/* Convert Try to Either containing JsonError or Output */
private def tryToEither[O](tryError: Try[O]): Either[JsonError, O] = {
    tryError match 
        case Failure(err) => Left(JsonError(err.getMessage))
        case Success(value) => Right(value)
}


/* Defines JSON parsing interface */
trait Reader[+O] {
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
        def read(s: String) = tryToEither(Try(up.read[O](s).nn))
    }
    
}


/* Defines JSON writing interface */
trait Writer[-I] {
    def write(input: I): Either[JsonError, String]
}

object Writer {
    /* Expose serialize object for calling write function */
    def apply[I](using serialize: Writer[I]) = serialize 

    /* Define upickle writer */
    type upickle[I] = up.Writer[I] 
    object upickle {
        inline def derived[I](using Mirror.Of[I], ClassTag[I]) = up.Writer.derived[I]
    }
    
    /* Delegate to upickle for JSON writing */
    given upickleWriter: [I: upickle] => Writer[I] {
        def write(input: I) = tryToEither(Try(up.write[I](input).nn))
    }

}
