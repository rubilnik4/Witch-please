package tarot.infrastructure.services

import tarot.infrastructure.services.photo.{FileStorageService, PhotoService, TelegramFileService}

trait TarotService {
  def photoService: PhotoService
  def fileStorageService: FileStorageService
  def telegramFileService: TelegramFileService
}