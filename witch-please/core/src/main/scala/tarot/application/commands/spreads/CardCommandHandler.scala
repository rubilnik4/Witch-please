package tarot.application.commands.spreads

import tarot.domain.models.TarotError
import tarot.domain.models.cards.{CardId, ExternalCard}
import tarot.layers.TarotEnv
import zio.ZIO

trait CardCommandHandler {
  def createCard (externalCard: ExternalCard): ZIO[TarotEnv, TarotError, CardId]
}