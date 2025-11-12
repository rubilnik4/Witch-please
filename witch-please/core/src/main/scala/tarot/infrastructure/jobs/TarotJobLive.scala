package tarot.infrastructure.jobs

import shared.infrastructure.services.files.FileStorageService
import shared.infrastructure.services.telegram.TelegramApiService
import tarot.infrastructure.jobs.spreads.SpreadJob
import tarot.infrastructure.services.authorize.AuthService
import tarot.infrastructure.services.photo.*

final case class TarotJobLive(
  spreadJob: SpreadJob
) extends TarotJob
