package tarot.api.dto.tarot.spreads

import shared.api.dto.tarot.projects.ProjectResponse
import shared.api.dto.tarot.spreads.SpreadResponse
import shared.api.dto.tarot.users.*
import shared.models.tarot.authorize.ClientType
import tarot.api.dto.tarot.photo.PhotoResponseMapper
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.ValidationError
import tarot.domain.models.authorize.{ExternalUser, User}
import tarot.domain.models.projects.Project
import tarot.domain.models.spreads.Spread
import zio.json.*
import zio.schema.*
import zio.{IO, ZIO}

object SpreadResponseMapper {
  def toResponse(spread: Spread): SpreadResponse =
    SpreadResponse(
      id = spread.id.id,
      projectId = spread.projectId.id,
      title = spread.title,
      cardCount = spread.cardCount,
      spreadStatus = spread.spreadStatus,
      coverPhoto = PhotoResponseMapper.toResponse(spread.coverPhoto),
      createdAt = spread.createdAt,
      scheduledAt = spread.scheduledAt,
      publishedAt = spread.publishedAt
    )
}