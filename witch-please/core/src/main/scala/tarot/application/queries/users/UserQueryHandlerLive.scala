package tarot.application.queries.users

import tarot.domain.models.TarotError
import tarot.domain.models.users.{Author, User}
import tarot.infrastructure.repositories.users.*
import tarot.layers.TarotEnv
import zio.ZIO

final class UserQueryHandlerLive(
  userRepository: UserRepository, 
  userProjectRepository: UserProjectRepository
) extends UserQueryHandler {
  override def getUserByClientId(clientId: String): ZIO[TarotEnv, TarotError, User] =
    for {
      _ <- ZIO.logDebug(s"Executing user query by clientId $clientId")
      
      userMaybe <- userRepository.getUserByClientId(clientId)
      user <- ZIO.fromOption(userMaybe)
        .orElseFail(TarotError.NotFound(s"user by clientId $clientId not found"))
    } yield user

  override def getAuthors: ZIO[TarotEnv, TarotError, List[Author]] =
    for {
      _ <- ZIO.logDebug(s"Executing authors query")

      authors <- userProjectRepository.getAuthors(1)
    } yield authors
}