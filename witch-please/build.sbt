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
  .aggregate(core, bot, common)
  .settings(
    name := "witch-root"
  )

lazy val common = project
  .in(file("common"))

lazy val core = project
  .dependsOn(common)
  .in(file("core"))

lazy val bot = project
  .dependsOn(core, common)
  .in(file("bot"))
