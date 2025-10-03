package tarot.infrastructure.services

import shared.infrastructure.services.files.FileStorageService
import shared.infrastructure.services.telegram.TelegramApiService
import tarot.infrastructure.services.authorize.AuthService
import tarot.infrastructure.services.photo.*
import tarot.infrastructure.services.users.UserService

trait TarotService {
  def authService: AuthService
  def photoService: PhotoService
  def fileStorageService: FileStorageService
  def telegramChannelService: TelegramApiService
}