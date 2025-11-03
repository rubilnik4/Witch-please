package tarot.application.queries.spreads

import tarot.domain.models.TarotError
import tarot.domain.models.projects.ProjectId
import tarot.domain.models.spreads.{Spread, SpreadId}
import tarot.layers.TarotEnv
import zio.ZIO

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
}