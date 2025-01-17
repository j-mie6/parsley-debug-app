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
    externalNpm := baseDirectory.value
  )
