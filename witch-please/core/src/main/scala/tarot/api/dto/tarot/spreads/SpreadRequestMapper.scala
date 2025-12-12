package tarot.api.dto.tarot.spreads

import shared.api.dto.tarot.spreads.*
import tarot.api.dto.tarot.photo.PhotoRequestMapper
import tarot.application.commands.spreads.commands.{CreateSpreadCommand, UpdateSpreadCommand}
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.ValidationError
import tarot.domain.models.authorize.UserId
import tarot.domain.models.spreads.SpreadId
import zio.{IO, ZIO}

object SpreadRequestMapper {
  def fromRequest(request: SpreadCreateRequest, userId: UserId): IO[TarotError, CreateSpreadCommand] =
    validate(request) *> toDomainCreate(request, userId)

  def fromRequest(request: SpreadUpdateRequest, spreadId: SpreadId): IO[TarotError, UpdateSpreadCommand] =
    validate(request) *> toDomainUpdate(request, spreadId)
    
  private def toDomainCreate(request: SpreadCreateRequest, userId: UserId) = 
    for {
      photo <- PhotoRequestMapper.fromRequest(request.photo)
    } yield CreateSpreadCommand(
        userId = userId,
        title = request.title,
        cardsCount = request.cardCount,
        description = request.description,
        photo = photo
      )

  private def toDomainUpdate(request: SpreadUpdateRequest, spreadId: SpreadId) =
    for {
      photo <- PhotoRequestMapper.fromRequest(request.photo)
    } yield UpdateSpreadCommand(
      spreadId = spreadId,
      title = request.title,
      cardCount = request.cardCount,
      description = request.description,
      photo = photo
    )
    
  private def validate(request: SpreadRequest) =
    for {
      _ <- ZIO.fail(ValidationError("title must not be empty")).when(request.title.trim.isEmpty)
      _ <- ZIO.fail(ValidationError("cardCount must be > 0")).when(request.cardCount <= 0)
      _ <- ZIO.fail(ValidationError("description must not be empty")).when(request.description.trim.isEmpty)
    } yield ()
}