package tarot.api.dto.tarot.spreads

import shared.api.dto.tarot.cards.{CardCreateRequest, CardUpdateRequest}
import shared.api.dto.tarot.spreads.*
import tarot.api.dto.tarot.photo.PhotoRequestMapper
import tarot.application.commands.cards.commands.*
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.ValidationError
import tarot.domain.models.cards.CardId
import tarot.domain.models.photo.PhotoSource
import tarot.domain.models.spreads.SpreadId
import zio.json.*
import zio.schema.*
import zio.{IO, ZIO}

import java.util.UUID

object CardRequestMapper {
  def fromRequest(request: CardCreateRequest, spreadId: SpreadId): IO[TarotError, CreateCardCommand] =
    validate(request) *> toDomainCreate(request, spreadId)

  def fromRequest(request: CardUpdateRequest, cardId: CardId): IO[TarotError, UpdateCardCommand] =
    validate(request) *> toDomainUpdate(request, cardId)
    
  private def toDomainCreate(request: CardCreateRequest, spreadId: SpreadId) =
    for {
      photo <- PhotoRequestMapper.fromRequest(request.photo)
    } yield CreateCardCommand(
      position = request.position,
      spreadId = spreadId,
      title = request.title,
      photo = photo
    )

  private def toDomainUpdate(request: CardUpdateRequest, cardId: CardId) =
    for {
      photo <- PhotoRequestMapper.fromRequest(request.photo)
    } yield UpdateCardCommand(
      cardId = cardId,
      title = request.title,
      photo = photo
    )
    
  private def validate(request: CardCreateRequest) = {
    for {
      _ <- ZIO.fail(ValidationError("position must be positive")).when(request.position < 0)
      _ <- ZIO.fail(ValidationError("title must not be empty")).when(request.title.trim.isEmpty)
    } yield ()
  }

  private def validate(request: CardUpdateRequest) = {
    for {
      _ <- ZIO.fail(ValidationError("title must not be empty")).when(request.title.trim.isEmpty)
    } yield ()
  }
}