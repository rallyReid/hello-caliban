import Dependencies._

ThisBuild / scalaVersion := "2.13.3"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.mewborne"
ThisBuild / organizationName := "mewborne"

lazy val root = (project in file("."))
  .settings(
    name := "fun-graphql",
    libraryDependencies ++= Seq(
        caliban,
        calibanAkka,
        zio,
        circe,
        scalaTest % Test
      )
  )