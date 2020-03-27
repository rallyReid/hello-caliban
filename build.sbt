import Dependencies._

ThisBuild / scalaVersion := "2.13.1"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.mewborne"
ThisBuild / organizationName := "mewborne"

lazy val root = (project in file("."))
  .settings(
    name := "fun-graphql",
    libraryDependencies ++= Seq(
        "com.github.ghostdogpr" %% "caliban" % "0.7.3",
        "com.github.ghostdogpr" %% "caliban-akka-http" % "0.7.3", // routes for akka-http
        "dev.zio" %% "zio" % "1.0.0-RC18-2",
        "de.heikoseeberger" %% "akka-http-circe" % "1.31.0",
        scalaTest % Test
      )
  )