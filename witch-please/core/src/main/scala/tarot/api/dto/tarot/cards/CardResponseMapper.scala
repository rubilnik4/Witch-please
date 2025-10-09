package tarot.api.dto.tarot.cards

import shared.api.dto.tarot.cards.CardResponse
import shared.api.dto.tarot.projects.ProjectResponse
import shared.api.dto.tarot.spreads.SpreadResponse
import shared.api.dto.tarot.users.*
import shared.models.tarot.authorize.ClientType
import tarot.api.dto.tarot.photo.PhotoResponseMapper
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.ValidationError
import tarot.domain.models.authorize.{ExternalUser, User}
import tarot.domain.models.cards.Card
import tarot.domain.models.projects.Project
import tarot.domain.models.spreads.Spread
import zio.json.*
import zio.schema.*
import zio.{IO, ZIO}

object CardResponseMapper {
  def toResponse(card: Card): CardResponse =
    CardResponse(
      id = card.id.id,
      spreadId = card.spreadId.id,
      description = card.description,
      photo = PhotoResponseMapper.toResponse(card.photo),
      createdAt = card.createdAt
    )
}