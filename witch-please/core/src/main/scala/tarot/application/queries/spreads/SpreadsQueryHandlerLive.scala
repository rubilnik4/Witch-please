package tarot.application.queries.spreads

import tarot.domain.models.TarotError
import tarot.domain.models.projects.ProjectId
import tarot.domain.models.spreads.{Spread, SpreadId}
import tarot.infrastructure.repositories.spreads.SpreadRepository
import tarot.layers.TarotEnv
import zio.ZIO

import java.time.Instant

final class SpreadsQueryHandlerLive(spreadRepository: SpreadRepository) extends SpreadQueryHandler {
  def getSpread(spreadId: SpreadId): ZIO[TarotEnv, TarotError, Spread] =
    for {
      _ <- ZIO.logInfo(s"Executing spread query by spreadId $spreadId")
      
      spread <- spreadRepository.getSpread(spreadId)
        .flatMap(ZIO.fromOption(_).orElseFail(TarotError.NotFound(s"Spread $spreadId not found")))
    } yield spread
  
  def getSpreads(projectId: ProjectId): ZIO[TarotEnv, TarotError, List[Spread]] =
    for {
      _ <- ZIO.logInfo(s"Executing spreads query by projectId $projectId")
      
      spreads <- spreadRepository.getSpreads(projectId)
    } yield spreads

  def getScheduleSpreads(deadline: Instant, limit: Int): ZIO[TarotEnv, TarotError, List[Spread]] =
    for {
      _ <- ZIO.logInfo(s"Executing ready spreads query by deadline $deadline")
      
      spreads <- spreadRepository.getScheduleSpreads(deadline, limit)
    } yield spreads
}