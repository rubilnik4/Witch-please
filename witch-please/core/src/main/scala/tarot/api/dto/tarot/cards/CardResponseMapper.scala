package tarot.api.dto.tarot.cards

import shared.api.dto.tarot.cards.CardResponse
import tarot.api.dto.tarot.photo.PhotoResponseMapper
import tarot.domain.models.TarotError
import tarot.domain.models.cards.Card

object CardResponseMapper {
  def toResponse(card: Card): CardResponse =
    CardResponse(
      id = card.id.id,
      index = card.index,
      spreadId = card.spreadId.id,
      description = card.title,
      photo = PhotoResponseMapper.toResponse(card.photo),
      createdAt = card.createdAt
    )
}