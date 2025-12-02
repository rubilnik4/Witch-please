package tarot.api.dto.tarot.spreads

import shared.api.dto.tarot.spreads.*
import tarot.api.dto.tarot.photo.PhotoRequestMapper
import tarot.application.commands.cards.commands.CreateCardCommand
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.ValidationError
import tarot.domain.models.photo.PhotoSource
import tarot.domain.models.spreads.SpreadId
import zio.json.*
import zio.schema.*
import zio.{IO, ZIO}

import java.util.UUID

object CardCreateRequestMapper {
  def fromRequest(request: CardCreateRequest, index: Int, spreadId: UUID): IO[TarotError, CreateCardCommand] =
    validate(request, index, spreadId) *> toDomain(request, index, spreadId)
  
  private def toDomain(request: CardCreateRequest, index: Int, spreadId: UUID) =
    for {
      photo <- PhotoRequestMapper.fromRequest(request.photo)
    } yield CreateCardCommand(
      index = index,
      spreadId = SpreadId(spreadId),
      title = request.title,
      photo = photo
    )

  private def validate(request: CardCreateRequest, index: Int, spreadId: UUID) = {
    for {
      _ <- ZIO.fail(ValidationError("index must be positive")).when(index < 0)
      _ <- ZIO.fail(ValidationError("description must not be empty")).when(request.title.trim.isEmpty)
    } yield ()
  }
}