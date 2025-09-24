package tarot.layers

import shared.infrastructure.telemetry.metrics.TelemetryMeter
import shared.infrastructure.telemetry.tracing.TelemetryTracing
import tarot.application.commands.TarotCommandHandler
import tarot.application.configurations.TarotConfig
import tarot.application.queries.TarotQueryHandler
import tarot.infrastructure.repositories.TarotRepository
import tarot.infrastructure.services.TarotService

trait TarotEnv {
  def config: TarotConfig
  def tarotService: TarotService
  def tarotRepository: TarotRepository
  def tarotCommandHandler: TarotCommandHandler
  def tarotQueryHandler: TarotQueryHandler
  def telemetryMeter: TelemetryMeter
  def telemetryTracing: TelemetryTracing
}
