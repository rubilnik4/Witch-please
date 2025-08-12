name := "witch-shared"

libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % "2.1.19",
  "dev.zio" %% "zio-streams" % "2.1.19",
  "dev.zio" %% "zio-json" % "0.7.43",
  "dev.zio" %% "zio-http" % "3.3.3",
  "dev.zio" %% "zio-logging" % "2.5.0",
  "dev.zio" %% "zio-opentelemetry" % "3.1.5",
  "com.softwaremill.sttp.client3" %% "core" % "3.11.0",
  "com.softwaremill.sttp.client3" %% "zio-json" % "3.11.0",
  "com.softwaremill.sttp.client3" %% "async-http-client-backend-zio" % "3.11.0",
  "io.opentelemetry" % "opentelemetry-sdk" % "1.51.0",
  "io.opentelemetry" % "opentelemetry-sdk-trace" % "1.51.0",
  "io.opentelemetry" % "opentelemetry-exporter-otlp" % "1.51.0",
  "io.opentelemetry" % "opentelemetry-exporter-logging-otlp" % "1.51.0",
  "io.opentelemetry" % "opentelemetry-exporter-prometheus" % "1.51.0-alpha",
  "io.opentelemetry.semconv" % "opentelemetry-semconv" % "1.34.0"
)