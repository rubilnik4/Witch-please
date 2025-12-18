package tarot.application.jobs.spreads

import tarot.domain.models.TarotError
import tarot.domain.models.cardOfDay.CardOfDayId
import tarot.domain.models.spreads.SpreadId

sealed trait PublishJobResult {
  def result: Either[TarotError, Unit]
}

object PublishJobResult {
  final case class Spread(id: SpreadId, result: Either[TarotError, Unit]) extends PublishJobResult
  final case class CardOfDay(id: CardOfDayId, result: Either[TarotError, Unit]) extends PublishJobResult
}