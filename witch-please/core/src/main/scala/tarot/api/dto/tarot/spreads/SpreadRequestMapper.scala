package tarot.api.dto.tarot.spreads

import shared.api.dto.tarot.spreads.*
import tarot.api.dto.tarot.photo.PhotoRequestMapper
import tarot.application.commands.spreads.commands.{CreateSpreadCommand, UpdateSpreadCommand}
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.ValidationError
import tarot.domain.models.authorize.UserId
import tarot.domain.models.photo.PhotoSource
import tarot.domain.models.projects.ProjectId
import tarot.domain.models.spreads.SpreadId
import zio.json.*
import zio.schema.*
import zio.{IO, ZIO}

import java.time.{Duration, Instant}
import java.util.UUID

object SpreadRequestMapper {
  def fromRequest(request: SpreadCreateRequest, userId: UserId): IO[TarotError, CreateSpreadCommand] =
    validate(request) *> toDomain(request, userId)

  def fromRequest(request: SpreadUpdateRequest, spreadId: SpreadId): IO[TarotError, UpdateSpreadCommand] =
    validate(request) *> toDomain(request, spreadId)
    
  private def toDomain(request: SpreadCreateRequest, userId: UserId) = 
    for {
      photo <- PhotoRequestMapper.fromRequest(request.photo)
    } yield CreateSpreadCommand(
        userId = userId,
        title = request.title,
        cardCount = request.cardCount,
        photo = photo
      )

  private def toDomain(request: SpreadUpdateRequest, spreadId: SpreadId) =
    for {
      photo <- PhotoRequestMapper.fromRequest(request.photo)
    } yield UpdateSpreadCommand(
      spreadId = spreadId,
      title = request.title,
      cardCount = request.cardCount,
      photo = photo
    )
    
  private def validate(request: SpreadRequest) =
    for {
      _ <- ZIO.fail(ValidationError("title must not be empty")).when(request.title.trim.isEmpty)
      _ <- ZIO.fail(ValidationError("cardCount must be > 0")).when(request.cardCount <= 0)
    } yield ()
}