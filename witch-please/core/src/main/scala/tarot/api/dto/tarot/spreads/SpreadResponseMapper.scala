package tarot.api.dto.tarot.spreads

import shared.api.dto.tarot.spreads.SpreadResponse
import tarot.api.dto.tarot.photo.PhotoResponseMapper
import tarot.domain.models.TarotError
import tarot.domain.models.spreads.Spread

object SpreadResponseMapper {
  def toResponse(spread: Spread): SpreadResponse =
    SpreadResponse(
      id = spread.id.id,
      title = spread.title,
      cardsCount = spread.cardsCount,
      description = spread.description,
      status = spread.status,
      photo = PhotoResponseMapper.toResponse(spread.photo),
      createdAt = spread.createdAt,
      scheduledAt = spread.scheduledAt,
      publishedAt = spread.publishedAt
    )
}