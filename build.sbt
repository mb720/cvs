name := """cvs"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

// Add this resolver first to avoid problems: https://github.com/mohiva/play-silhouette-seed/issues/20#issuecomment-75367376
resolvers := ("Atlassian Releases" at "https://maven.atlassian.com/public/") +: resolvers.value

routesGenerator := InjectedRoutesGenerator

// Without this, only main.less is applied to the websites; differently named .less files are ignored.
includeFilter in(Assets, LessKeys.less) := "*.less"

//pipelineStages := Seq(uglify, digest, gzip)
// Two percent signs mean that the Scala version, such as _2.11, is appended to the artifact ID
libraryDependencies ++= Seq(
  jdbc,
  cache,
  // Used to prevent CSRF attacks
  filters,
  ws,
  evolutions,
  // Persistence
  "com.typesafe.slick" %% "slick" % "3.1.1",
  "org.postgresql" % "postgresql" % "9.4.1207.jre7",
  // Lets us use Twitter Bootstrap forms and buttons in our Twirl templates
  "com.adrianhurt" %% "play-bootstrap3" % "0.4.5-P24",
  // User authentication
  "com.mohiva" %% "play-silhouette" % "3.0.4",
  // Transforms Markdown to HTML
  "org.planet42" %% "laika-core" % "0.5.1",
  // Manages client-side dependencies such as jQuery
  "org.webjars" %% "webjars-play" % "2.4.0-1",
  "org.webjars" % "jquery" % "2.1.4",
  "org.webjars" % "font-awesome" % "4.5.0"
)

scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs
  "-feature", // Emit warning and location for usages of features that should be imported explicitly
  "-unchecked", // Enable additional warnings where generated code depends on assumptions
  "-Xfatal-warnings", // Fail the compilation if there are any warnings
  "-Xlint", // Enable recommended additional warnings
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver
  "-Ywarn-dead-code", // Warn when dead code is identified
  "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures
  "-Ywarn-nullary-override", // Warn when non-nullary overrides nullary, e.g. def foo() over def foo
  "-Ywarn-numeric-widen" // Warn when numerics are widened
)

scalacOptions in (Compile, doc) ++= Seq(
  "-no-link-warnings" // Per default scaladoc doesn't allow us to link to classes in libraries. Suppress those warnings
)

herokuAppName in Compile := "ezgportaltest"
