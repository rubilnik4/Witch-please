package tarot.infrastructure.services.telegram

import shared.models.files.*
import tarot.domain.models.TarotError
import tarot.domain.models.cards.Card
import tarot.domain.models.cardsOfDay.CardOfDay
import tarot.domain.models.spreads.Spread
import tarot.layers.TarotEnv
import zio.ZIO

trait TelegramPublishService {
  def publishSpread(chatId: Long, spread: Spread, cards: List[Card]): ZIO[TarotEnv, TarotError, Unit]
  def publishCardOfDay(chatId: Long, cardOfDay: CardOfDay): ZIO[TarotEnv, TarotError, Unit]
}
