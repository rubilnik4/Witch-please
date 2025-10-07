package tarot.application.queries.spreads

import tarot.domain.models.TarotError
import tarot.domain.models.authorize.User
import tarot.domain.models.projects.Project
import tarot.domain.models.spreads.Spread
import tarot.layers.TarotEnv
import zio.ZIO

final class SpreadsQueryHandlerLive extends SpreadsQueryHandler {
  def handle(query: SpreadsQuery): ZIO[TarotEnv, TarotError, List[Spread]] =
    for {
      _ <- ZIO.logInfo(s"Executing spreads query by projectId ${query.projectId}")

      repository <- ZIO.serviceWith[TarotEnv](_.tarotRepository.spreadRepository)
      spreads <- repository.getSpreads(query.projectId)
    } yield spreads
}