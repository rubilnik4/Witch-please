package tarot.application.queries.spreads

import tarot.domain.models.TarotError
import tarot.domain.models.spreads.{Spread, SpreadId}
import tarot.domain.models.users.UserId
import tarot.infrastructure.repositories.spreads.SpreadRepository
import tarot.infrastructure.repositories.users.UserProjectRepository
import tarot.layers.TarotEnv
import zio.ZIO

import java.time.Instant

final class SpreadsQueryHandlerLive(
  spreadRepository: SpreadRepository,
  userProjectRepository: UserProjectRepository                                 
) extends SpreadQueryHandler {
  
  override def getSpread(spreadId: SpreadId): ZIO[TarotEnv, TarotError, Spread] =
    for {
      _ <- ZIO.logInfo(s"Executing spread query by spreadId $spreadId")
      
      spread <- spreadRepository.getSpread(spreadId)
        .flatMap(ZIO.fromOption(_).orElseFail(TarotError.NotFound(s"Spread $spreadId not found")))
        .tapError(_ => ZIO.logError(s"Spread $spreadId not found"))
    } yield spread

  override def getSpreads(userId: UserId): ZIO[TarotEnv, TarotError, List[Spread]] =
    for {
      _ <- ZIO.logInfo(s"Executing spreads query by userId $userId")

      projectIds <- userProjectRepository.getProjectIds(userId)
      projectId <- ZIO.fromOption(projectIds.headOption).orElseFail(TarotError.NotFound(s"No project found for user $userId"))
        .tapError(_ => ZIO.logError(s"No project found for user $userId"))
      
      spreads <- spreadRepository.getSpreads(projectId)
    } yield spreads

  override def getScheduledSpreads(deadline: Instant, limit: Int): ZIO[TarotEnv, TarotError, List[Spread]] =
    for {
      _ <- ZIO.logInfo(s"Executing ready to publish spreads query by deadline $deadline")      
      
      spreads <- spreadRepository.getScheduledSpreads(deadline, limit)
    } yield spreads
}