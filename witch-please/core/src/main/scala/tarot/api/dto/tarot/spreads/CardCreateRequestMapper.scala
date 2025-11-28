package tarot.api.dto.tarot.spreads

import shared.api.dto.tarot.spreads.*
import tarot.api.dto.tarot.photo.PhotoRequestMapper
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.ValidationError
import tarot.domain.models.cards.ExternalCard
import tarot.domain.models.photo.PhotoFile
import tarot.domain.models.spreads.SpreadId
import zio.json.*
import zio.schema.*
import zio.{IO, ZIO}

import java.util.UUID

object CardCreateRequestMapper {
  def fromRequest(request: CardCreateRequest, index: Int, spreadId: UUID): IO[TarotError, ExternalCard] =
    validate(request, index, spreadId) *> toDomain(request, index, spreadId)
  
  private def toDomain(request: CardCreateRequest, index: Int, spreadId: UUID) =
    for {
      photo <- PhotoRequestMapper.fromRequest(request.photo)
    } yield ExternalCard(
      index = index,
      spreadId = SpreadId(spreadId),
      description = request.description,
      coverPhoto = photo
    )

  private def validate(request: CardCreateRequest, index: Int, spreadId: UUID) = {
    for {
      _ <- ZIO.fail(ValidationError("index must be positive")).when(index < 0)
      _ <- ZIO.fail(ValidationError("description must not be empty")).when(request.description.trim.isEmpty)
    } yield ()
  }
}