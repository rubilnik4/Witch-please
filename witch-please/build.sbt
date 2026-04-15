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
  .enablePlugins(JavaAppPackaging)
  .dependsOn(
    shared % "compile->compile;test->test"
  )
  .in(file("core"))
  .settings(
    Compile / mainClass := Some("tarot.MainApp")
  )

lazy val bot = project
  .enablePlugins(JavaAppPackaging)
  .dependsOn(
    shared % "compile->compile;test->test",
    core % "test->test"
  )
  .in(file("bot"))
  .settings(
    Compile / mainClass := Some("bot.MainApp")
  )
