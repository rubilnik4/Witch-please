package tarot.infrastructure.services

import shared.infrastructure.services.storage.{FileStorageService, ResourceFileService}
import shared.infrastructure.services.telegram.TelegramApiService
import tarot.infrastructure.services.authorize.AuthService
import tarot.infrastructure.services.health.HealthService
import tarot.infrastructure.services.photo.*
import tarot.infrastructure.services.telegram.TelegramPublishService

final case class TarotServiceLive(
  authService: AuthService,
  photoService: PhotoService,
  resourceFileService: ResourceFileService,
  fileStorageService: FileStorageService,
  telegramApiService: TelegramApiService,
  telegramPublishService: TelegramPublishService,
  healthService: HealthService
) extends TarotService 
