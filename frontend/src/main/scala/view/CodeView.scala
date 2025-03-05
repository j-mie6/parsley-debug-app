package view

import com.raquo.laminar.api.L.*
import com.raquo.laminar.api.features.unitArrows

import controller.viewControllers.CodeViewController

trait FileObject {
    lazy val render: Element
}

case class File(name: String) extends FileObject {
    lazy val render: Element = div(
        className := "code-view-file",
        i(marginRight.px := 5, className := "bi bi-file-earmark-code-fill"),
        name,
    )
}

case class Directory(name: String, contents: List[FileObject], expanded: Var[Boolean]) extends FileObject {
    lazy val render: Element = div(
        className := "code-view-directory",
        div(
            className := "code-view-directory-header",
            div(
                i(marginRight.px := 5, className := "bi bi-folder-fill"),
                name.init,
                marginRight.px := 10
            ),
            child <-- expanded.signal.splitBoolean(_ => i(className := "bi bi-caret-down-fill"), _ => i(className := "bi bi-caret-left-fill")),
            onClick --> expanded.invert()
        ),
        div(
            className := "code-view-directory-contents",
            contents.map(_.render),
            cls("expanded") <-- expanded,
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
      * @param input The paths.
      * @return A file object.
      */
    def fromPaths(input: List[String]): FileObject = input match
        case Nil => RootDirectory(Nil)
        case head :: Nil => RootDirectory(List(File(head.drop(head.lastIndexWhere(chr => chr == '/' || chr == '\\')))))
        case files@(head :: next) => shortestFilePrefix(head, next) match {
            case ("", _) => RootDirectory(fromPathsList(files))
            case (prefix, prefixFiles) => RootDirectory(fromPathsList(prefixFiles.map(prefix.drop(prefix.init.lastIndexWhere(chr => chr == '/' || chr == '\\')).tail + _)))
        }
    

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
     * This function is functional programming at it's finest.
     * 
     * @param file The file to compare against.
     * @param files List of the other files to compare.
     * @return (prefix, list of files with prefix (with prefix stripped))
     */
    private def shortestFilePrefix(file: String, files: List[String]): (String, List[String]) = file.indexWhere(chr => chr == '/' || chr == '\\') match
        case -1 => ("", file :: files)
        case index =>
            val prefix: String = file.take(index + 1)
            files.filter(_.startsWith(prefix)) match
                case Nil => ("", file :: files)
                case prefixFiles =>
                    val strippedFiles: List[String] = prefixFiles.map(_.stripPrefix(prefix))
                    val strippedFile: String = file.stripPrefix(prefix)
                    shortestFilePrefix(strippedFile, strippedFiles) match
                        case ("", file :: files) => (prefix, strippedFile :: strippedFiles)
                        case (fst, snd) => if snd.length < strippedFiles.length then (prefix, strippedFile :: strippedFiles) else (prefix + fst, snd)

    /**
      * Turn a list of filePaths into a list of file objects to represent them.
      *
      * @param input The file paths
      * @return
      */
    private def fromPathsList(input: List[String]): List[FileObject] = input match
        case Nil => Nil
        case head :: Nil => File(head) :: Nil
        case head :: next => shortestFilePrefix(head, next) match {
            case ("", _) => File(head) :: fromPathsList(next)
            case (prefix, files) => Directory(prefix, fromPathsList(files), Var(true)) :: fromPathsList(input.filter(!_.startsWith(prefix)))
        }    
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
        "scala/root/dir1/file.scala",
        "scala/root/dir2/file2.scala",
        "scala/root/dir2/file3.scala"
    )

    def apply(): HtmlElement = {
        val maybeFileContents: Signal[Option[Element]] = CodeViewController.getCurrentFile.splitOption((_, signal) => renderFile(signal))
        // val maybeFileExplorer: Signal[Option[Element]] = CodeViewController.getFileInformation.splitOption((codeInfo, _) => renderFileExplorer(codeInfo.info.keySet.toList))
        val maybeFileExplorer: Signal[Option[Element]] = CodeViewController.getFileInformation.mapTo(Some(exampleFs)).splitOption((ls, _) => renderFileExplorer(ls))

        div(
            className := "code-view-main-view",

            renderFileExplorer(exampleFs),
            // child <-- maybeFileExplorer.map(_.getOrElse(defaultFileExplorer)),
            child <-- maybeFileContents.map(_.getOrElse(defaultFile)),
        )
    }
}
