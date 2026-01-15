package tarot.api.dto.tarot.cardOfDay

import shared.api.dto.tarot.cardsOfDay.CardOfDayResponse
import shared.api.dto.tarot.cards.CardResponse
import shared.api.dto.tarot.photo.PhotoResponse
import shared.models.tarot.cardOfDay.CardOfDayStatus
import tarot.api.dto.tarot.photo.PhotoResponseMapper
import tarot.domain.models.TarotError
import tarot.domain.models.cardsOfDay.CardOfDay
import tarot.domain.models.cards.Card

import java.time.Instant
import java.util.UUID

object CardOfDayResponseMapper {
  def toResponse(cardOfDay: CardOfDay): CardOfDayResponse =
    CardOfDayResponse(
      id = cardOfDay.id.id,
      cardId = cardOfDay.cardId.id,
      spreadId = cardOfDay.spreadId.id,
      description = cardOfDay.description,
      status = cardOfDay.status,
      photo =  PhotoResponseMapper.toResponse(cardOfDay.photo),
      createdAt = cardOfDay.createdAt,
      scheduledAt = cardOfDay.scheduledAt,
      publishedAt = cardOfDay.publishedAt
    )
}