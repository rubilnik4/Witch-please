package tarot.api.dto.tarot.cardOfDay

import shared.api.dto.tarot.cards.*
import tarot.api.dto.tarot.photo.PhotoRequestMapper
import tarot.application.commands.cardOfDay.commands.CreateCardOfDayCommand
import tarot.application.commands.cards.commands.*
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.ValidationError
import tarot.domain.models.cards.CardId
import tarot.domain.models.spreads.SpreadId
import zio.{IO, ZIO}

object CardOfDayRequestMapper {
  def fromRequest(request: CardOfDayCreateRequest, spreadId: SpreadId): IO[TarotError, CreateCardOfDayCommand] =
    validateCreate(request) *> toDomainCreate(request, spreadId)
    
  private def toDomainCreate(request: CardOfDayCreateRequest, spreadId: SpreadId) =
    for {
      photo <- PhotoRequestMapper.fromRequest(request.photo)
    } yield CreateCardOfDayCommand(
      cardId = CardId(request.cardId),
      spreadId = spreadId,
      description = request.description,
      photo = photo
    )
    
  private def validateCreate(request: CardOfDayCreateRequest) = {
    for {
      _ <- ZIO.fail(ValidationError("description must not be empty")).when(request.description.trim.isEmpty)
    } yield ()
  }
}