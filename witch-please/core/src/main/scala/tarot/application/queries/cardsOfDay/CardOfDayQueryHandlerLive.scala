package tarot.application.queries.cardsOfDay

import tarot.domain.models.TarotError
import tarot.domain.models.cardOfDay.CardOfDay
import tarot.domain.models.cards.{Card, CardId}
import tarot.domain.models.spreads.SpreadId
import tarot.infrastructure.repositories.cardOfDay.CardOfDayRepository
import tarot.infrastructure.repositories.cards.CardRepository
import tarot.layers.TarotEnv
import zio.ZIO

import java.time.Instant

final class CardOfDayQueryHandlerLive(cardOfDayRepository: CardOfDayRepository) extends CardOfDayQueryHandler {

  override def getCardOfDay(spreadId: SpreadId): ZIO[TarotEnv, TarotError, CardOfDay] =
    for {
      _ <- ZIO.logInfo(s"Executing card of day query by spreadId $spreadId")

      cardOfDay <- cardOfDayRepository.getCardOfDay(spreadId)
        .flatMap(ZIO.fromOption(_).orElseFail(TarotError.NotFound(s"Card of day by spreadId $spreadId not found")))
        .tapError(_ => ZIO.logError(s"Card of day by spreadId $spreadId not found"))
    } yield cardOfDay

  
  override def getScheduledCardsOfDay(deadline: Instant, limit: Int): ZIO[TarotEnv, TarotError, List[CardOfDay]] =
    for {
      _ <- ZIO.logInfo(s"Executing ready to publish cards of day query by deadline $deadline")

      spreads <- cardOfDayRepository.getScheduledCardsOfDay(deadline, limit)
    } yield spreads
}