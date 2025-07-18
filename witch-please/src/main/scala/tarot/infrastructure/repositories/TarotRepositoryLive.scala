package tarot.infrastructure.repositories

import tarot.infrastructure.repositories.projects.ProjectRepository
import tarot.infrastructure.repositories.spreads.SpreadRepository
import tarot.infrastructure.repositories.users.{UserProjectRepository, UserRepository}
import tarot.infrastructure.services.photo.{FileStorageService, PhotoService, TelegramFileService}

final case class TarotRepositoryLive(
                                      userRepository: UserRepository,
                                      userProjectRepository: UserProjectRepository,
                                      projectRepository: ProjectRepository,
                                      spreadRepository: SpreadRepository
) extends TarotRepository 
