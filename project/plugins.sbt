resolvers += Resolver.sonatypeRepo("snapshots")

// Command matrix helper from https://github.com/indoorvivants/sbt-commandmatrix
// addSbtPlugin("com.indoorvivants" % "sbt-commandmatrix" % "0.0.5")
// addSbtPlugin("com.eed3si9n" % "sbt-projectmatrix" % "0.8.0")

// ScalablyTyped plugin
addSbtPlugin("org.scalablytyped.converter" % "sbt-converter" % "1.0.0-beta44")

// Scala JS
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.16.0")
// addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.21.1+36-2d9cbce2-SNAPSHOT")