import org.scalajs.linker.interface.Report
import sys.process.*

val Scala213 = "2.13.14"
val Scala212 = "2.12.18"
val Scala3 = "3.6.3"

val scalatestVersion = "3.2.19"

val Version = "0.1"

Global / onChangedBuildSource := ReloadOnSourceChanges
Global / excludeLintKeys += dillFrontend / Compile / stMinimize


/* Handle OS specific usage of commands */
def convertCmd(cmd: String): String = (
    sys.props("os.name").toLowerCase() match {
        case osName if osName contains "windows" => "cmd /C " ++ cmd
        case _ => cmd
    }
) 


/* Global project settings */
inThisBuild(List(
    version := Version,
    organization := "com.github.j-mie6",
    organizationName := "Parsley Debug App Contributors <https://github.com/j-mie6/parsley-debug-app/graphs/contributors>",
    startYear := Some(2025),
    licenses := List("BSD-3-Clause" -> url("https://opensource.org/licenses/BSD-3-Clause")),
    developers := List(
        Developer("j-mie6", "Jamie Willis", "", url("https://github.com/j-mie6/parsley-debug-app")),
        Developer("Riley-horrix", "Riley Horrix", "", url("https://github.com/j-mie6/parsley-debug-app")),
        Developer("aniket1101", "Aniket Gupta", "", url("https://github.com/j-mie6/parsley-debug-app")),
        Developer("PriyanshC", "Priyansh Chugh", "", url("https://github.com/j-mie6/parsley-debug-app")),
        Developer("Aito0", "Alejandro Perez Fadon", "", url("https://github.com/j-mie6/parsley-debug-app")),
        Developer("AdamW1087", "Adam Watson", "", url("https://github.com/j-mie6/parsley-debug-app")),
        Developer("josh-ja-walker", "Josh Walker", "", url("https://github.com/j-mie6/parsley-debug-app"))
    ),
    versionScheme := Some("early-semver"),
    scalaVersion := Scala3,
))

/* Setup project dependencies */
lazy val commonSettings = Seq(
    resolvers ++= Opts.resolver.sonatypeOssReleases, /* Will speed up MiMA during fast back-to-back releases */
    resolvers ++= Opts.resolver.sonatypeOssSnapshots, /* Needed during flux periods */

    /* Dependencies required throughout project */
    libraryDependencies ++= Seq(
        "org.scalatest" %%% "scalatest" % scalatestVersion % Test,
        "org.scala-lang" %% "scala3-compiler" % "3.3.3"
    ),

    /* Test settings */
    Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oI"),
    Test / parallelExecution := false,
)

lazy val isRelease = sys.env.get("RELEASE").contains("true") /* Compile in release mode (not dev) */

/* Setup for Laminar */
lazy val dillFrontend = project
    .in(file("frontend"))
    .enablePlugins(ScalaJSPlugin, ScalablyTypedConverterExternalNpmPlugin)
    .settings(
        /* Scala JS/ScalablyTyped settings */
        scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.ESModule)),
        scalaJSUseMainModuleInitializer := true,
        stUseScalaJsDom := true,
        stIgnore += "type-fest",
        Compile / stMinimize := Selection.AllExcept("types", "tauri-apps"),

        /* Frontend settings */        
        name := "dill-frontend",
        commonSettings, 
        libraryDependencies ++= Seq( /* Extra dependencies required for frontend */
            "com.raquo" %%% "laminar" % "17.2.0",
            "com.vladsch.flexmark" % "flexmark-all" % "0.61.26",
            "com.lihaoyi" %%% "upickle" % "4.1.0"
        ),

        /* Run npm to link with ScalablyTyped */
        externalNpm := {
            convertCmd("npm").!
            println()
            baseDirectory.value.getParentFile()
        },

        /* Compile the frontend */
        Compile / packageSrc / mappings ++= {
            val base = (Compile / sourceManaged).value
            val files = (Compile / managedSources).value
            files.map { f => (f, f.relativeTo(base).get.getPath) }
        }
    )


/* Report frontend build setup */
lazy val reportFrontend = taskKey[(Report, File)]("")
ThisBuild / reportFrontend := {
    if (isRelease) {
        println("Building in release mode")
    } else {
        println("Building in quick compile mode. To build in release mode, set RELEASE environment variable to \"true\"")
    }

    (dillFrontend / Compile / fullLinkJS).value.data -> (dillFrontend / Compile / fullLinkJS / scalaJSLinkerOutputDirectory).value
}


/* Build Dill frontend */
lazy val buildFrontend = taskKey[Map[String, File]]("Build the Scala Laminar frontend.")

buildFrontend := {
    val (report, fm) = reportFrontend.value
    val outDir = (ThisBuild / baseDirectory).value / "static"
    
    IO.listFiles(fm)
        .map { file =>
            val (name, ext) = file.baseAndExt
            val out = outDir / (name + "." + ext)

            IO.copyFile(file, out)

            file.name -> out
        }
        .toMap
}


/* Build Tauri backend */
lazy val buildBackend = taskKey[Unit]("Build Tauri app into packages and executables.")

buildBackend := {
    convertCmd("npm run tauri build").!
}

/* Run Tauri backend in dev mode */
lazy val runBackend = taskKey[Unit]("Run Tauri app in development mode.")

runBackend := {    
    convertCmd("npm run tauri dev").!
}


/* Build frontend and backend into executables */
val build = taskKey[Unit]("Build the project into packages and executables.")

build := {
    buildFrontend.value
    buildBackend.value
}


/* Run project - install dependencies, build frontend then run backend */
run := {
    setup.value
    buildFrontend.value
    runBackend.value
}


/* Setup required dependencies */
lazy val setup = taskKey[Unit]("Install required dependencies.")

setup := {
    convertCmd("npm install").!
}


/* Build project in Docker */
val dockerBuild = taskKey[Unit]("Build the project onto a Docker machine, running the application.")

dockerBuild := {
    print("Copying Files... ")
    "scp -o StrictHostKeyChecking=no -r -P 2222 ./src ./backend root@localhost:/home >> logs".!
    println("done")

    println("Building frontend... ")
    "echo \"cd /home && sbt buildFrontend\" | ssh -o StrictHostKeyChecking=no -p 2222 root@localhost >> logs 2>&1".!
    println("Done")
}


/* Clean all generated files */
val cleanHard = taskKey[Unit]("Remove generated files and dependencies.")

cleanHard := {
    print("Removing npm dependencies... ")
    "rm -rf node_modules/".!
    println("done")

    print("Removing Scala files... ")
    "rm -rf .metals/".!
    "rm -rf .bloop/".!
    "rm -rf .bsp/".!
    "rm -rf .scala-build/".!
    println("done")
    
    print("Removing targets... ")
    "rm -rf static/".!
    println("done")

    print("Removing backend targets... ")
    "rm -rf backend/target/".!
    println("done")
    
    print("Removing frontend targets... ")
    "rm -rf frontend/target/".!
    println("done")

    /* Apply default clean */
    clean.value
}

