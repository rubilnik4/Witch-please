package tarot.layers

import tarot.application.commands.TarotCommandHandler
import tarot.application.configurations.TarotConfig
import tarot.application.queries.TarotQueryHandler
import tarot.application.telemetry.metrics.TarotMeter
import tarot.application.telemetry.tracing.TarotTracing
import tarot.infrastructure.repositories.TarotRepository
import tarot.infrastructure.services.TarotService

trait TarotEnv {
  def config: TarotConfig
  def tarotService: TarotService
  def tarotRepository: TarotRepository
  def tarotCommandHandler: TarotCommandHandler
  def tarotQueryHandler: TarotQueryHandler
  def tarotMeter: TarotMeter
  def tarotTracing: TarotTracing
}
