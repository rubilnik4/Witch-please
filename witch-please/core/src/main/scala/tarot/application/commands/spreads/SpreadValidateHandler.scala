package tarot.application.commands.spreads

import shared.models.tarot.spreads.SpreadStatus
import tarot.domain.models.TarotError
import tarot.domain.models.spreads.Spread
import zio.ZIO

object SpreadValidateHandler {
  def validateModifyStatus(spread: Spread): ZIO[Any, TarotError, Unit] =
    for {
      _ <- ZIO.unless(SpreadStatus.isModify(spread.status)) {
        ZIO.logError(s"Spread ${spread.id} already published, couldn't be modify") *>
          ZIO.fail(TarotError.Conflict(s"Spread ${spread.id} already published, couldn't be modify"))
      }
    } yield ()
}
