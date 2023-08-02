ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.11"

lazy val root = (project in file("."))
  .settings(
    name := "producer_consumer_actors"
  )

libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % "2.8.0"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.4.7"
