ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.4"

lazy val root = (project in file("."))
  .settings(
    name := "ik_http_server"
  )
libraryDependencies ++= Seq(
    "io.circe" %% "circe-core" % "0.15.0-M1",
    "io.circe" %% "circe-generic" % "0.15.0-M1",
    "io.circe" %% "circe-parser" % "0.15.0-M1"
)
