name := "ScalaJS-Tauri-App"

version := "0.1.0-SNAPSHOT"

scalaVersion := "3.3.4"

organization := "com.example"

moduleName := "tauri-app"

lazy val settingsForScalablyTyped = Seq(
  stOutputPackage := "com.example.tauri",
  stMinimize := Selection.AllExcept("@types","@tauri-apps/api"),
  stUseScalaJsDom := true,
  stIgnore += "type-fest"
)

lazy val linkerSettings = Seq(
  scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.ESModule)),
  scalaJSUseMainModuleInitializer := true
)

lazy val tauri = project
  .in(file("."))
  .enablePlugins(ScalaJSPlugin, ScalablyTypedConverterExternalNpmPlugin)
  .settings(
    // Compile / npmDependencies ++= Seq(
    //   "@tauri-apps/api" -> "2.2.0"
    // ),
    settingsForScalablyTyped,
    linkerSettings,
    libraryDependencies += "com.raquo" %%% "laminar" % "17.2.0",
    externalNpm := baseDirectory.value,
    Compile / packageSrc / mappings ++= {
      val base = (Compile / sourceManaged).value
      val files = (Compile / managedSources).value
      files.map { f => (f, f.relativeTo(base).get.getPath) }
    }
  )

lazy val isRelease = sys.env.get("RELEASE").contains("true")

lazy val buildFrontend = taskKey[Map[String, File]]("")

import org.scalajs.linker.interface.Report
lazy val frontendReport = taskKey[(Report, File)]("")

ThisBuild / frontendReport := {
  if (isRelease)
    (tauri / Compile / fullLinkJS).value.data ->
      (tauri / Compile / fullLinkJS / scalaJSLinkerOutputDirectory).value
  else
    (tauri / Compile / fastLinkJS).value.data ->
      (tauri / Compile / fastLinkJS / scalaJSLinkerOutputDirectory).value
}

buildFrontend := {
  val (report, fm) = frontendReport.value
  val outDir = (ThisBuild / baseDirectory).value / "static"
  IO.listFiles(fm)
    .toList
    .map { file =>
      val (name, ext) = file.baseAndExt
      val out = outDir / (name + "." + ext)

      IO.copyFile(file, out)

      file.name -> out
    }
    .toMap
}

Global / onChangedBuildSource := ReloadOnSourceChanges

