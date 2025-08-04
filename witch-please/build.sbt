inThisBuild(Seq(
  scalaVersion := "3.4.2",
  logLevel := Level.Warn,
  scalacOptions ++= Seq(
    "-Yretain-trees",
    "-Xfatal-warnings"
  ),
  semanticdbEnabled := true,
  semanticdbVersion := scalafixSemanticdb.revision
))

lazy val root = (project in file("."))
  .aggregate(core)
  .settings(
    name := "witch-please-root"
  )

lazy val core = project
  .in(file("core"))