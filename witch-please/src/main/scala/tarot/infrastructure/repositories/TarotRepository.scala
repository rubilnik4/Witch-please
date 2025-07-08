package tarot.infrastructure.repositories

import tarot.infrastructure.repositories.auth.AuthRepository
import tarot.infrastructure.repositories.spreads.SpreadRepository
import tarot.infrastructure.services.photo.{FileStorageService, PhotoService, TelegramFileService}

trait TarotRepository {
  def authRepository: AuthRepository
  def spreadRepository: SpreadRepository
}