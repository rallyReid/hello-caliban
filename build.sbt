import Dependencies._

ThisBuild / scalaVersion := "2.13.1"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.mewborne"
ThisBuild / organizationName := "mewborne"

lazy val root = (project in file("."))
  .settings(
    name := "fun-graphql",
    libraryDependencies ++= Seq(
        "com.github.ghostdogpr" %% "caliban" % "0.9.2",
        "com.github.ghostdogpr" %% "caliban-akka-http" % "0.9.2",
        "dev.zio" %% "zio" % "1.0.3",
        "de.heikoseeberger" %% "akka-http-circe" % "1.35.0",
        scalaTest % Test
      )
  )