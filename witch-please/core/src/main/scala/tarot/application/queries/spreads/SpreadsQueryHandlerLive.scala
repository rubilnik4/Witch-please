package tarot.application.queries.spreads

import tarot.domain.models.TarotError
import tarot.domain.models.projects.ProjectId
import tarot.domain.models.spreads.{Spread, SpreadId}
import tarot.layers.TarotEnv
import zio.ZIO

import java.time.Instant

final class SpreadsQueryHandlerLive extends SpreadsQueryHandler {
  def getSpread(spreadId: SpreadId): ZIO[TarotEnv, TarotError, Spread] =
    for {
      _ <- ZIO.logInfo(s"Executing spread query by spreadId $spreadId")

      repository <- ZIO.serviceWith[TarotEnv](_.tarotRepository.spreadRepository)
      spread <- repository.getSpread(spreadId)
        .flatMap(ZIO.fromOption(_).orElseFail(TarotError.NotFound(s"Spread $spreadId not found")))
    } yield spread
  
  def getSpreads(projectId: ProjectId): ZIO[TarotEnv, TarotError, List[Spread]] =
    for {
      _ <- ZIO.logInfo(s"Executing spreads query by projectId $projectId")

      repository <- ZIO.serviceWith[TarotEnv](_.tarotRepository.spreadRepository)
      spreads <- repository.getSpreads(projectId)
    } yield spreads

  def getScheduleSpreads(deadline: Instant, limit: Int): ZIO[TarotEnv, TarotError, List[Spread]] =
    for {
      _ <- ZIO.logInfo(s"Executing ready spreads query by deadline $deadline")

      repository <- ZIO.serviceWith[TarotEnv](_.tarotRepository.spreadRepository)
      spreads <- repository.getScheduleSpreads(deadline, limit)
    } yield spreads
}