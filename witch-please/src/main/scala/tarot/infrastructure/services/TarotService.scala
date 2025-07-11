package tarot.infrastructure.services

import tarot.infrastructure.services.auth.AuthService
import tarot.infrastructure.services.photo.{FileStorageService, PhotoService, TelegramFileService}

trait TarotService {
  def authService: AuthService
  def photoService: PhotoService
  def fileStorageService: FileStorageService
  def telegramFileService: TelegramFileService
}