package tarot.api.dto.tarot.cardOfDay

import shared.api.dto.tarot.cardsOfDay.CardOfDayResponse
import tarot.api.dto.tarot.photo.PhotoResponseMapper
import tarot.domain.models.cardsOfDay.CardOfDay

object CardOfDayResponseMapper {
  def toResponse(cardOfDay: CardOfDay): CardOfDayResponse =
    CardOfDayResponse(
      id = cardOfDay.id.id,      
      cardId = cardOfDay.cardId.id,
      spreadId = cardOfDay.spreadId.id,
      title = cardOfDay.title,
      description = cardOfDay.description,
      status = cardOfDay.status,
      photo =  PhotoResponseMapper.toResponse(cardOfDay.photo),
      createdAt = cardOfDay.createdAt,
      scheduledAt = cardOfDay.scheduledAt,
      publishedAt = cardOfDay.publishedAt
    )
}