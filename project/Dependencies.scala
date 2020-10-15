import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.8"
  lazy val caliban = "com.github.ghostdogpr" %% "caliban" % "0.9.2"
  lazy val calibanAkka = "com.github.ghostdogpr" %% "caliban-akka-http" % "0.9.2"
  lazy val zio = "dev.zio" %% "zio" % "1.0.3"
  lazy val circe = "de.heikoseeberger" %% "akka-http-circe" % "1.35.0"
}
