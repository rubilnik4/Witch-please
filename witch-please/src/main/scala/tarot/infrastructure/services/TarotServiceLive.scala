package tarot.infrastructure.services

import tarot.infrastructure.services.auth.AuthService
import tarot.infrastructure.services.photo.{FileStorageService, PhotoService, TelegramFileService}
import tarot.infrastructure.services.users.UserService

final case class TarotServiceLive(
  authService: AuthService,
  userService: UserService,                               
  photoService: PhotoService,
  fileStorageService: FileStorageService,
  telegramFileService: TelegramFileService,
) extends TarotService 
