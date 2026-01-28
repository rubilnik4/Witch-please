package tarot.infrastructure.services

import shared.infrastructure.services.storage.FileStorageService
import shared.infrastructure.services.telegram.TelegramApiService
import tarot.infrastructure.services.authorize.AuthService
import tarot.infrastructure.services.photo.*

final case class TarotServiceLive(
                                   authService: AuthService,
                                   photoService: PhotoService,
                                   fileStorageService: FileStorageService,
                                   telegramApiService: TelegramApiService,
) extends TarotService 
