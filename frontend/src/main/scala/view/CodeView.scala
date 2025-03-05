package view

import com.raquo.laminar.api.L.*
import com.raquo.laminar.api.features.unitArrows

import controller.viewControllers.CodeViewController

trait FileObject {
    lazy val render: Element
}

case class File(name: String, fullPath: String) extends FileObject {
    lazy val render: Element = div(
        className := "code-view-file",
        i(marginRight.px := 5, className := "bi bi-file-earmark-code-fill"),
        name,

        onClick.mapTo(fullPath) --> CodeViewController.requestSourceFile,
    )
}

case class Directory(name: String, contents: List[FileObject], expanded: Var[Boolean]) extends FileObject {
    lazy val render: Element = div(
        className := "code-view-directory",
        cls("expanded") <-- expanded.signal.invert,
        div(
            className := "code-view-directory-header",
            child <-- expanded.signal.splitBoolean(_ => i(className := "bi bi-caret-right-fill"), _ => i(className := "bi bi-caret-down-fill")),
            i(marginRight.px := 5, marginLeft.px := 5, className := "bi bi-folder-fill"),
            name.init,
            onClick --> expanded.invert()
        ),
        div(
            className := "code-view-directory-contents",
            contents.map(_.render),
            cls("expanded") <-- expanded.signal,
        )
    )
}

case class RootDirectory(contents: List[FileObject]) extends FileObject {
    lazy val render: Element = div(
        className := "code-view-root-directory",
        contents.map(_.render),
    )
}

case object FileObject {
    /**
      * Generate a File object from a list of paths.
      * 
      * This function is a 'wrapper' for the list version, however it strips all
      * but the final directory, as to compress the (possibly) absolute path down
      * to its main parts.
      *
      * @param input The paths.
      * @return A file object.
      */
    def fromPaths(input: List[String]): FileObject = RootDirectory(input match
        case Nil => Nil
        case head :: Nil => List(File(head.drop(head.lastIndexWhere(charIsSlash)), head))
        case files@(head :: next) => shortestFilePrefix(head, next) match {
            case ("", _) => fromPathsList(files, "")
            case (prefix, prefixFiles) => {
                println("fromPaths prefix : " + prefix + " files : " + prefixFiles)
                // We still want the parent directory
                val splitIndex: Int = prefix.init.lastIndexWhere(charIsSlash(_))
                val parentDir: String = prefix.drop(splitIndex).tail
                val restOfPath: String = prefix.take(splitIndex + 1)
                fromPathsList(prefixFiles.map(parentDir + _), restOfPath)
            }
        }
    )

    // def splitWhere(str: String, cond: Char => Boolean): List[String] = str.indexWhere(cond) match
    //     case -1: Int => List(str)
    //     case n: Int => str.take(n) :: splitWhere(str.drop(n), cond)
        
    // def fromPaths(input: List[String]): FileObject = input.map(splitWhere(_, charIsSlash))

    def charIsSlash(chr: Char): Boolean = chr == '/' || chr == '\\'

    /**
     * Returns the shortest common file prefix of this file with all other files.
     * 
     * I.e.
     *  file = "root/dir1/test.scala"
     *  files = ["root/dir1/test2.scala", "root/dir2/test.scala"]
     * 
     * returns
     *  ("root", ["dir1/test.scala", "dir1/test2.scala", "dir2/test.scala"]) 
     * 
     * This function is functional programming at it's finest (don't ask how it works)
     * 
     * @param file The file to compare against.
     * @param files List of the other files to compare.
     * @return (prefix, list of files with prefix (with prefix stripped))
     */
    private def shortestFilePrefix(file: String, files: List[String]): (String, List[String]) = file.indexWhere(charIsSlash(_)) match
        case -1 => ("", file :: files)
        case index =>
            val prefix: String = file.take(index + 1)
            println("shrFilePth prefix : " + prefix)
            println("shrFilePth files : " + (file :: files))
            files.filter(_.startsWith(prefix)) match
                case Nil => ("", file :: files)
                case prefixFiles =>
                    val strippedFiles: List[String] = prefixFiles.map(_.stripPrefix(prefix))
                    val strippedFile: String = file.stripPrefix(prefix)
                    shortestFilePrefix(strippedFile, strippedFiles) match
                        case ("", file :: files) => (prefix, strippedFile :: strippedFiles)
                        case (fst, snd) => if snd.length < (strippedFiles.length + 1) then (prefix, strippedFile :: strippedFiles) else (prefix + fst, snd)

    /**
      * Turn a list of filePaths into a list of file objects to represent them.
      *
      * @param input The file paths
      * @return
      */
    private def fromPathsList(input: List[String], prefix: String): List[FileObject] = {println("fromPathsList with input : " + input + " and prefix : " + prefix);input match
        case Nil => Nil
        case head :: Nil => File(head, prefix + head) :: Nil
        case head :: next => shortestFilePrefix(head, next) match {
            case ("", _) => File(head, prefix + head) :: fromPathsList(next, prefix)
            case (thisPrefix, files) => Directory(thisPrefix, fromPathsList(files, prefix + thisPrefix), Var(true)) :: fromPathsList(input.filter(!_.startsWith(thisPrefix)), prefix)
        }    }
}

object CodeView {
    def renderFile(contents: Signal[String]): Element = {
        div(
            className := "code-view-file-view",
            text <-- contents
        )
    }

    def renderFileExplorer(fileInfo: List[String]): Element = {
        println("From strings!")
        val fileSystem: FileObject = FileObject.fromPaths(fileInfo)
        div(
            className := "code-view-file-explorer",
            fileSystem.render
        )
    }

    lazy val defaultFileExplorer: Element = div(
        className := "code-view-file-explorer",
        "No File Explorer"
    )

    lazy val defaultFile: Element = div(
        className := "code-view-file-view",
        "No File"
    )

    val exampleFs: List[String] = List(
        "/dir/root/file.scala",
        "/dir/root2/file2.scala",
        "/dir/root2/file3.scala",
        "/dir/root/file3.scala"
    )

    def apply(): HtmlElement = {
        val maybeFileContents: Signal[Option[Element]] = CodeViewController.getCurrentFile.splitOption((_, signal) => renderFile(signal))
        val maybeFileExplorer: Signal[Option[Element]] = CodeViewController.getFileInformation.splitOption((codeInfo, _) => renderFileExplorer(codeInfo.info.keySet.toList))

        div(
            className := "code-view-main-view",

            renderFileExplorer(exampleFs),
            // child <-- maybeFileExplorer.map(_.getOrElse(defaultFileExplorer)),
            child <-- maybeFileContents.map(_.getOrElse(defaultFile)),
        )
    }
}
