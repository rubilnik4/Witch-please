package tarot.infrastructure.services

import tarot.infrastructure.services.auth.AuthService
import tarot.infrastructure.services.photo.{FileStorageService, PhotoService, TelegramFileService}

final case class TarotServiceLive(
  authService: AuthService,
  photoService: PhotoService,
  fileStorageService: FileStorageService,
  telegramFileService: TelegramFileService,
) extends TarotService 
