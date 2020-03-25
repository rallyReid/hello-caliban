import Dependencies._

ThisBuild / scalaVersion     := "2.13.1"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.mewborne"
ThisBuild / organizationName := "mewborne"

lazy val root = (project in file("."))
  .settings(
    name := "fun-graphql",
    libraryDependencies ++= Seq(
      "com.github.ghostdogpr" %% "caliban" % "0.7.0",
      "com.github.ghostdogpr" %% "caliban-akka-http" % "0.7.0" // routes for akka-http
      "dev.zio" %% "zio" % "1.0.0-RC18-2",
      scalaTest % Test
    )
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
