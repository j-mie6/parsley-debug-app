package view.error

import com.raquo.laminar.api.L.*

import view.error.*

object ErrorHandler {
    def handleError(error: String): HtmlElement = {
        // error match {
        //     case "LockFailed" => BreakingError(error)
        //     case "SerialiseFailed" => BreakingError(error)
        //     case "TreeNotFound" => BreakingError(error)
        //     case "CreateDirFailed" => BreakingError(error)
        //     case "NodeNotFound" => BreakingError(error)
	    //     case "WriteTreeFailed" => BreakingError(error)
	    //     case "ReadDirFailed" => BreakingError(error)
	    //     case "ReadPathFailed" => BreakingError(error)
	    //     case "StringContainsInvalidUnicode" => PopupError(error)
	    //     case "SuffixNotFound" => BreakingError(error)
	    //     case "ReadFileFailed" => ReadFileFailedError()
	    //     case "DeserialiseFailed" => DeserialiseFailedError()
        //     case _ => UnknownError() 
        // }
        BreakingError(error)
    }
}