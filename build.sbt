ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.11"

lazy val root = (project in file("."))
  .settings(
    name := "Humidity_Sensor_Statictics"
  )

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % "test"