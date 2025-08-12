package tarot.infrastructure.services

import shared.infrastructure.services.TelegramApiService
import tarot.infrastructure.services.authorize.AuthService
import tarot.infrastructure.services.photo.*

final case class TarotServiceLive(
  authService: AuthService,
  photoService: PhotoService,
  fileStorageService: FileStorageService,
  telegramApiService: TelegramApiService,
) extends TarotService 
