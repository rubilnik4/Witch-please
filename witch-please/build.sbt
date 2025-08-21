inThisBuild(Seq(
  scalaVersion := "3.4.2",
  logLevel := Level.Warn,
  scalacOptions ++= Seq(
    "-deprecation",   // показывать устаревшие API
    "-feature",       // показывать скрытые language features
    "-unchecked",     // проверки pattern matching
    "-Werror"         // все варнинги -> ошибки (важно!)
))

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
  .dependsOn(core, shared)
  .in(file("bot"))
