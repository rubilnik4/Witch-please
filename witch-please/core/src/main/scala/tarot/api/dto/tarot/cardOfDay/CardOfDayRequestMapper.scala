package tarot.api.dto.tarot.cardOfDay

import shared.api.dto.tarot.cardsOfDay.{CardOfDayCreateRequest, CardOfDayRequest, CardOfDayUpdateRequest}
import shared.api.dto.tarot.cards.*
import tarot.api.dto.tarot.photo.PhotoRequestMapper
import tarot.application.commands.cardsOfDay.commands.*
import tarot.application.commands.cards.commands.*
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.ValidationError
import tarot.domain.models.cards.CardId
import tarot.domain.models.cardsOfDay.CardOfDayId
import tarot.domain.models.spreads.SpreadId
import zio.{IO, ZIO}

object CardOfDayRequestMapper {
  def fromRequest(request: CardOfDayCreateRequest, spreadId: SpreadId): IO[TarotError, CreateCardOfDayCommand] =
    validate(request) *> toCreateCommand(request, spreadId)

  def fromRequest(request: CardOfDayUpdateRequest, cardOfDayId: CardOfDayId): IO[TarotError, UpdateCardOfDayCommand] =
    validate(request) *> toUpdateCommand(request, cardOfDayId)
    
  private def toCreateCommand(request: CardOfDayCreateRequest, spreadId: SpreadId) =
    for {
      photo <- PhotoRequestMapper.fromRequest(request.photo)
    } yield CreateCardOfDayCommand(
      cardId = CardId(request.cardId),
      spreadId = spreadId,
      description = request.description,
      photo = photo
    )

  private def toUpdateCommand(request: CardOfDayUpdateRequest, cardOfDayId: CardOfDayId) =
    for {
      photo <- PhotoRequestMapper.fromRequest(request.photo)
    } yield UpdateCardOfDayCommand(
      cardOfDayId = cardOfDayId,
      cardId = CardId(request.cardId),
      description = request.description,
      photo = photo
    )
    
  private def validate(request: CardOfDayRequest) = {
    for {
      _ <- ZIO.fail(ValidationError("description must not be empty")).when(request.description.trim.isEmpty)
    } yield ()
  }
}