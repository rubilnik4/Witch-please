package tarot.application.jobs

import shared.infrastructure.services.storage.FileStorageService
import shared.infrastructure.services.telegram.TelegramApiService
import tarot.application.jobs.spreads.PublishJob
import tarot.infrastructure.services.authorize.AuthService
import tarot.infrastructure.services.photo.*
import tarot.infrastructure.services.users.UserService

trait TarotJob {
  def publishJob: PublishJob
}