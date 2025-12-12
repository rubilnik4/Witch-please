package tarot.api.dto.tarot.cards

import shared.api.dto.tarot.cards.*
import tarot.api.dto.tarot.photo.PhotoRequestMapper
import tarot.application.commands.cards.commands.*
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.ValidationError
import tarot.domain.models.cards.CardId
import tarot.domain.models.spreads.SpreadId
import zio.{IO, ZIO}

object CardRequestMapper {
  def fromRequest(request: CardCreateRequest, spreadId: SpreadId): IO[TarotError, CreateCardCommand] =
    validateCreate(request) *> toDomainCreate(request, spreadId)

  def fromRequest(request: CardUpdateRequest, cardId: CardId): IO[TarotError, UpdateCardCommand] =
    validate(request) *> toDomainUpdate(request, cardId)
    
  private def toDomainCreate(request: CardCreateRequest, spreadId: SpreadId) =
    for {
      photo <- PhotoRequestMapper.fromRequest(request.photo)
    } yield CreateCardCommand(
      position = request.position,
      spreadId = spreadId,
      title = request.title,
      description = request.description,
      photo = photo
    )

  private def toDomainUpdate(request: CardUpdateRequest, cardId: CardId) =
    for {
      photo <- PhotoRequestMapper.fromRequest(request.photo)
    } yield UpdateCardCommand(
      cardId = cardId,
      title = request.title,
      description = request.description,
      photo = photo
    )
    
  private def validateCreate(request: CardCreateRequest) = {
    for {
      _ <- ZIO.fail(ValidationError("position must be positive")).when(request.position < 0)
      _ <- validate(request)
    } yield ()
  }

  private def validate(request: CardRequest) = {
    for {
      _ <- ZIO.fail(ValidationError("title must not be empty")).when(request.title.trim.isEmpty)
      _ <- ZIO.fail(ValidationError("description must not be empty")).when(request.description.trim.isEmpty)
    } yield ()
  }
}