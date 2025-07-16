package tarot.infrastructure.repositories

import tarot.infrastructure.repositories.spreads.SpreadRepository
import tarot.infrastructure.repositories.users.{UserAccessRepository, UserRepository}
import tarot.infrastructure.services.photo.{FileStorageService, PhotoService, TelegramFileService}

final case class TarotRepositoryLive(
  userRepository: UserRepository,
  userAccessRepository: UserAccessRepository,
  spreadRepository: SpreadRepository
) extends TarotRepository 
