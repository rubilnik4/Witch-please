inThisBuild(Seq(
  scalaVersion := "3.4.2",
  logLevel := Level.Warn,
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-unchecked",
    "-Werror"
)))

lazy val root = (project in file("."))
  .aggregate(core, bot, shared)
  .settings(
    name := "witch-root"
  )

lazy val shared = project
  .in(file("shared"))

lazy val core = project
  .dependsOn(shared)
  .in(file("core"))

lazy val bot = project
  .dependsOn(
    core   % "compile->compile;test->test",
    shared % "compile->compile"
  )
  .in(file("bot"))
