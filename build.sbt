import Dependencies.{scalaTest, scalastic}

name := "log-analyzer"

version := "1.0"

scalaVersion := "3.3.3"

mainClass := Some("Main")

scalaVersion := Versions.scala3
libraryDependencies ++= Seq(scalaTest, scalastic)

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.12.0",
  "org.typelevel" %% "cats-effect" % "3.5.4",
  "org.typelevel" %% "cats-effect-testing-scalatest" % "1.5.0" % Test,

  "co.fs2" %% "fs2-core" % "3.10.2",
  "co.fs2" %% "fs2-io" % "3.10.2",

  "org.http4s" %% "http4s-core" % "0.23.27",

  "com.github.scopt" %% "scopt" % "4.1.0"
)
