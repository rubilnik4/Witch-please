package tarot.infrastructure.services

import shared.infrastructure.services.TelegramApiService
import tarot.infrastructure.services.authorize.AuthService
import tarot.infrastructure.services.photo.*
import tarot.infrastructure.services.users.UserService

trait TarotService {
  def authService: AuthService
  def photoService: PhotoService
  def fileStorageService: FileStorageService
  def telegramApiService: TelegramApiService
}