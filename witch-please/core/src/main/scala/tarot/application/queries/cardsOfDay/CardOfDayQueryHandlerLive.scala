package tarot.application.queries.cardsOfDay

import tarot.domain.models.TarotError
import tarot.domain.models.cards.CardId
import tarot.domain.models.cardsOfDay.{CardOfDay, CardOfDayId}
import tarot.domain.models.spreads.SpreadId
import tarot.infrastructure.repositories.cardsOfDay.CardOfDayRepository
import tarot.layers.TarotEnv
import zio.ZIO

import java.time.Instant

final class CardOfDayQueryHandlerLive(cardOfDayRepository: CardOfDayRepository) extends CardOfDayQueryHandler {

  override def getCardOfDay(cardOfDayId: CardOfDayId): ZIO[TarotEnv, TarotError, CardOfDay] =
    for {
      _ <- ZIO.logDebug(s"Executing card of day query by id $cardOfDayId")

      cardOfDay <- cardOfDayRepository.getCardOfDay(cardOfDayId)
        .flatMap(ZIO.fromOption(_).orElseFail(TarotError.NotFound(s"Card of day by id $cardOfDayId not found")))
        .tapError(_ => ZIO.logError(s"Card of day by id $cardOfDayId not found"))
    } yield cardOfDay

  
  override def getCardOfDayBySpread(spreadId: SpreadId): ZIO[TarotEnv, TarotError, CardOfDay] =
    for {
      _ <- ZIO.logDebug(s"Executing card of day query by spreadId $spreadId")

      cardOfDay <- cardOfDayRepository.getCardOfDayBySpread(spreadId)
        .flatMap(ZIO.fromOption(_).orElseFail(TarotError.NotFound(s"Card of day by spreadId $spreadId not found")))
        .tapError(_ => ZIO.logError(s"Card of day by spreadId $spreadId not found"))
    } yield cardOfDay

  override def getCardOfDayBySpreadOption(spreadId: SpreadId): ZIO[TarotEnv, TarotError, Option[CardOfDay]] =
    for {
      _ <- ZIO.logDebug(s"Executing card of day option query by spreadId $spreadId")

      cardOfDay <- cardOfDayRepository.getCardOfDayBySpread(spreadId)
    } yield cardOfDay

  override def getCardOfDayByCard(cardId: CardId): ZIO[TarotEnv, TarotError, CardOfDay] =
    for {
      _ <- ZIO.logDebug(s"Executing card of day query by cardId $cardId")

      cardOfDay <- cardOfDayRepository.getCardOfDayByCard(cardId)
        .flatMap(ZIO.fromOption(_).orElseFail(TarotError.NotFound(s"Card of day by cardId $cardId not found")))
        .tapError(_ => ZIO.logError(s"Card of day by cardId $cardId not found"))
    } yield cardOfDay

  override def getCardOfDayByCardOption(cardId: CardId): ZIO[TarotEnv, TarotError, Option[CardOfDay]] =
    for {
      _ <- ZIO.logDebug(s"Executing card of day option query by cardId $cardId")

      cardOfDay <- cardOfDayRepository.getCardOfDayByCard(cardId)
    } yield cardOfDay
    
  override def getScheduledCardsOfDay(deadline: Instant, limit: Int): ZIO[TarotEnv, TarotError, List[CardOfDay]] =
    for {
      _ <- ZIO.logDebug(s"Executing ready to publish cards of day query by deadline $deadline")

      spreads <- cardOfDayRepository.getScheduledCardsOfDay(deadline, limit)
    } yield spreads
}