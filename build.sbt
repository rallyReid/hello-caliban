import Dependencies._

ThisBuild / scalaVersion     := "2.13.1"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.mewborne"
ThisBuild / organizationName := "mewborne"

lazy val root = (project in file("."))
  .settings(
    name := "fun-graphql",
    libraryDependencies ++= Seq(
      "com.github.ghostdogpr" %% "caliban" % "0.5.0",
      "com.github.ghostdogpr" %% "caliban-http4s" % "0.5.0",    // routes for http4s
      "com.github.ghostdogpr" %% "caliban-cats" % "0.5.0",
      scalaTest % Test
    )
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
