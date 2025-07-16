package tarot.infrastructure.repositories

import tarot.infrastructure.repositories.spreads.SpreadRepository
import tarot.infrastructure.repositories.users.{UserAccessRepository, UserRepository}

trait TarotRepository {
  def userRepository: UserRepository
  def userAccessRepository: UserAccessRepository
  def spreadRepository: SpreadRepository
}