package controller.viewControllers

import com.raquo.laminar.api.L.*
import com.raquo.airstream.core.*

import controller.tauri.Tauri
import controller.tauri.Command.RequestSourceFile

import model.CodeFileInformation

object CodeViewController {
    /* The file string to be rendered by the code view. */
    val getFileInformation: Signal[Option[CodeFileInformation]] = MainViewController.parserInfo.map(_.map(CodeFileInformation.apply))

    val unloadFile: Observer[Unit] = Observer {
        _ => currentFile.set(None)
    }

    /* The file string to be rendered by the code view. */
    private val currentFile: Var[Option[String]] = Var(None)

    val getCurrentFile: Signal[Option[String]] = currentFile.signal

    val setCurrentFile: Observer[Option[String]] = currentFile.writer

    val requestSourceFile: Observer[String] = Observer[String](filePath => Tauri.invoke(RequestSourceFile, filePath))
}
