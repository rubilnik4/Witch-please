package tarot.layers

import shared.infrastructure.telemetry.metrics.TelemetryMeter
import shared.infrastructure.telemetry.tracing.TelemetryTracing
import tarot.application.commands.TarotCommandHandler
import tarot.application.configurations.TarotConfig
import tarot.application.jobs.TarotJob
import tarot.application.queries.TarotQueryHandler
import tarot.infrastructure.repositories.spreads.SpreadRepository
import tarot.infrastructure.services.TarotService

final case class TarotEnvLive(
  config: TarotConfig,
  tarotService: TarotService,
  tarotJob: TarotJob,
  tarotCommandHandler: TarotCommandHandler,
  tarotQueryHandler: TarotQueryHandler,
  telemetryMeter: TelemetryMeter,
  telemetryTracing: TelemetryTracing
) extends TarotEnv
