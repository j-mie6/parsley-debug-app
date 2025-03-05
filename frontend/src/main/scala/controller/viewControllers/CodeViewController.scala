package controller.viewControllers

import com.raquo.laminar.api.L.*
import com.raquo.airstream.core.*

import controller.tauri.Tauri
import model.CodeFileInformation

object CodeViewController {
    /* The file string to be rendered by the code view. */
    private val codeFileInformation: Var[Option[CodeFileInformation]] = Var(None)

    val getFileInformation: Signal[Option[CodeFileInformation]] = codeFileInformation.signal

    val setFileInformation: Observer[Option[CodeFileInformation]] = codeFileInformation.writer


    /* The file string to be rendered by the code view. */
    private val currentFile: Var[Option[String]] = Var(None)

    val getCurrentFile: Signal[Option[String]] = currentFile.signal

    val setCurrentFile: Observer[Option[String]] = currentFile.writer
}
