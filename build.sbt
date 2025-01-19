val Scala213 = "2.13.14"
val Scala212 = "2.12.18"
val Scala3 = "3.3.3"

val scalatestVersion = "3.2.19"

val Version = "0.1"

Global / onChangedBuildSource := ReloadOnSourceChanges

inThisBuild(List(
  // tlBaseVersion := Version,
  version := Version,
  organization := "com.github.j-mie6",
  organizationName := "Parsley Debug App Contributors <https://github.com/j-mie6/parsley-debug-app/graphs/contributors>",
  startYear := Some(2024),
  licenses := List("BSD-3-Clause" -> url("https://opensource.org/licenses/BSD-3-Clause")),
  developers := List(
    tlGitHubDev("j-mie6", "Jamie Willis"),
    tlGitHubDev("Riley-horrix", "Riley Horrix"),
    tlGitHubDev("aniket1101", "Aniket Gupta"),
    tlGitHubDev("PriyanshC", "Priyansh Chung"),
    tlGitHubDev("Aito0", "Alejandro Perez Fadon"),
    tlGitHubDev("AdamW1087", "Adam Watson"),
    tlGitHubDev("josh-ja-walker", "Josh Walker")
  ),
  versionScheme := Some("early-semver"),
  scalaVersion := Scala3,
))

lazy val commonSettings = Seq(
  resolvers ++= Opts.resolver.sonatypeOssReleases, // Will speed up MiMA during fast back-to-back releases
  resolvers ++= Opts.resolver.sonatypeOssSnapshots, // needed during flux periods

  libraryDependencies ++= Seq(
    "org.scalatest"     %%% "scalatest"     % scalatestVersion % Test
  ),

  Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oI"),
  Test / parallelExecution := false,
)


lazy val dillFrontend = project
  .in(file("."))
  .enablePlugins(ScalaJSPlugin, ScalablyTypedConverterExternalNpmPlugin)
  .settings(
    scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.ESModule)),
    scalaJSUseMainModuleInitializer := true,
    stUseScalaJsDom := true,
    stIgnore += "type-fest",
    // "@types", "@tauri-apps/api"
    Compile / stMinimize := Selection.AllExcept("types", "tauri-apps"),
    name := "dill-frontend",
    commonSettings,
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
    (dillFrontend / Compile / fullLinkJS).value.data ->
      (dillFrontend / Compile / fullLinkJS / scalaJSLinkerOutputDirectory).value
  else
    (dillFrontend / Compile / fastLinkJS).value.data ->
      (dillFrontend / Compile / fastLinkJS / scalaJSLinkerOutputDirectory).value
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
