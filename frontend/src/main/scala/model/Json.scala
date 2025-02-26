package model.json

import scala.util.{Try, Failure, Success}
import scala.deriving.Mirror
import scala.reflect.ClassTag

import upickle.default as up
import model.errors.MalformedJSON
import model.errors.DillException
import controller.errors.ErrorController


/* Defines JSON parsing interface */
trait Reader[+O] {
    def read(s: String): Either[DillException, O]
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
        def read(s: String): Either[DillException, O] = {
            Try(up.read[O](s).nn) match
                case Success(value) => Right(value)
                case Failure(err) => Left(MalformedJSON)
        }
    }

    /* Read null value or "null" to () or fail with JsonError */
    given unitReader: Reader[Unit] {
        //FIXME: check == null and == "null" fail indeterminately
        def read(s: String): Either[DillException, Unit] = Either.cond(s == null || s == "null", (), MalformedJSON)
    }

}


/* Defines JSON writing interface */
trait Writer[-I] {
    def write(input: I): Either[DillException, String]
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
        def write(input: I): Either[DillException, String] = {
            Try(up.write[I](input).nn) match
                case Success(value) => Right(value)
                case Failure(err) => Left(MalformedJSON)
        }
    }

    /* Write Unit type as "null" */
    given unitWriter: Writer[Unit] {
        def write(input: Unit): Either[DillException, String] = Right("null")
    }

}
