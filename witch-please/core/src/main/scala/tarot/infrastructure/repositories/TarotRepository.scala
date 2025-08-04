package tarot.infrastructure.repositories

import tarot.infrastructure.repositories.projects.ProjectRepository
import tarot.infrastructure.repositories.spreads.SpreadRepository
import tarot.infrastructure.repositories.users.{UserProjectRepository, UserRepository}

trait TarotRepository {
  def userRepository: UserRepository
  def userProjectRepository: UserProjectRepository
  def projectRepository: ProjectRepository
  def spreadRepository: SpreadRepository
}