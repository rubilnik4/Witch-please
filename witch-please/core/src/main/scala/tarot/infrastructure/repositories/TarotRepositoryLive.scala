package tarot.infrastructure.repositories

import common.infrastructure.services.TelegramFileService
import tarot.infrastructure.repositories.projects.ProjectRepository
import tarot.infrastructure.repositories.spreads.SpreadRepository
import tarot.infrastructure.repositories.users.{UserProjectRepository, UserRepository}
import tarot.infrastructure.services.photo.{FileStorageService, PhotoService}

final case class TarotRepositoryLive(
                                      userRepository: UserRepository,
                                      userProjectRepository: UserProjectRepository,
                                      projectRepository: ProjectRepository,
                                      spreadRepository: SpreadRepository
) extends TarotRepository 
