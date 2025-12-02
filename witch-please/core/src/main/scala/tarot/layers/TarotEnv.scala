package tarot.layers

import shared.infrastructure.telemetry.metrics.TelemetryMeter
import shared.infrastructure.telemetry.tracing.TelemetryTracing
import tarot.application.commands.TarotCommandHandler
import tarot.application.configurations.TarotConfig
import tarot.application.jobs.TarotJob
import tarot.application.queries.TarotQueryHandler
import tarot.infrastructure.services.TarotService

trait TarotEnv {
  def config: TarotConfig
  def services: TarotService
  def jobs: TarotJob
  def commandHandlers: TarotCommandHandler
  def queryHandlers: TarotQueryHandler
  def telemetryMeter: TelemetryMeter
  def telemetryTracing: TelemetryTracing
}
