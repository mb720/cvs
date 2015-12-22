name := """cvs"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

routesGenerator := InjectedRoutesGenerator

//pipelineStages := Seq(uglify, digest, gzip)
libraryDependencies ++= Seq(
  jdbc,
  cache,
  // Used to prevent CSRF attacks
  filters,
  ws,
  evolutions,
  "com.typesafe.slick" %% "slick" % "3.0.0",
  "org.postgresql" % "postgresql" % "9.4-1204-jdbc42",
  "com.adrianhurt" % "play-bootstrap3_2.11" % "0.4.4-P24",
  // Dependency injection
  "net.codingwell" %% "scala-guice" % "4.0.0",
  // User authentication
  "com.mohiva" % "play-silhouette_2.11" % "3.0.4",
  "net.ceedubs" %% "ficus" % "1.1.2",
  // Manages client-side dependencies such as jQuery
  "org.webjars" %% "webjars-play" % "2.4.0-1",
  "org.webjars" % "jquery" % "2.1.4",
  "org.webjars" % "font-awesome" % "4.5.0"
)

herokuAppName in Compile := "ezgportal"

scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  "-Xlint", // Enable recommended additional warnings.
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
  "-Ywarn-nullary-override", // Warn when non-nullary overrides nullary, e.g. def foo() over def foo.
  "-Ywarn-numeric-widen" // Warn when numerics are widened.
)

