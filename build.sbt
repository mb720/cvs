name := """cvs"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

routesGenerator := InjectedRoutesGenerator

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "com.typesafe.slick" %% "slick"      % "3.0.0",
  "org.postgresql" % "postgresql" % "9.4-1204-jdbc42",
  "com.adrianhurt" % "play-bootstrap3_2.11" % "0.4.4-P24"
)

herokuAppName in Compile := "cvs"
