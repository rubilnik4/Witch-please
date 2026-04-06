package tarot.infrastructure.services

import shared.infrastructure.services.storage.*
import shared.infrastructure.services.telegram.TelegramApiService
import tarot.infrastructure.services.authorize.AuthService
import tarot.infrastructure.services.photo.*
import tarot.infrastructure.services.telegram.TelegramPublishService

trait TarotService {
  def authService: AuthService
  def photoService: PhotoService
  def resourceFileService: ResourceFileService
  def fileStorageService: FileStorageService
  def telegramApiService: TelegramApiService
  def telegramPublishService: TelegramPublishService
}
