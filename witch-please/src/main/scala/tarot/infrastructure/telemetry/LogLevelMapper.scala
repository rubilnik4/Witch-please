package tarot.infrastructure.telemetry

import zio.LogLevel

object LogLevelMapper {
  def parseLogLevel(str: String): Option[LogLevel] =
    str.toLowerCase match {
      case "trace" => Some(LogLevel.Trace)
      case "debug" => Some(LogLevel.Debug)
      case "info" => Some(LogLevel.Info)
      case "warning" => Some(LogLevel.Warning)
      case "error" => Some(LogLevel.Error)
      case "fatal" => Some(LogLevel.Fatal)
      case _ => None
    }
}
