name := "witch-bot"

libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % "2.1.19",
  "dev.zio" %% "zio-streams" % "2.1.19",
  "dev.zio" %% "zio-json" % "0.7.43",
  "dev.zio" %% "zio-http" % "3.3.3",
  "dev.zio" %% "zio-logging" % "2.5.0",
  "com.softwaremill.sttp.tapir" %% "tapir-core" % "1.11.36",
  "com.softwaremill.sttp.tapir" %% "tapir-json-zio" % "1.11.36",
  "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server" % "1.11.36",
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % "1.11.36",
  "dev.zio" %% "zio-test" % "2.1.19" % Test,
  "dev.zio" %% "zio-test-sbt" % "2.1.19" % Test,
  "dev.zio" %% "zio-http-testkit" % "3.3.3" % Test,
  "com.dimafeng" %% "testcontainers-scala-postgresql" % "0.43.0" % Test
)

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
