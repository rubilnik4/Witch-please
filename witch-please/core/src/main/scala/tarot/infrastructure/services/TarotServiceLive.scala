package tarot.infrastructure.services

import tarot.infrastructure.services.authorize.AuthService
import tarot.infrastructure.services.photo.{FileStorageService, PhotoService}

final case class TarotServiceLive(
  authService: AuthService,                           
  photoService: PhotoService,
  fileStorageService: FileStorageService,
  telegramFileService: TelegramFileService,
) extends TarotService 
