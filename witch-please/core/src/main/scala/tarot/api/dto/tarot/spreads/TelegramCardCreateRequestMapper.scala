package tarot.api.dto.tarot.spreads

import shared.api.dto.tarot.spreads.*
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.ValidationError
import tarot.domain.models.cards.ExternalCard
import tarot.domain.models.photo.ExternalPhoto
import tarot.domain.models.spreads.SpreadId
import zio.json.*
import zio.schema.*
import zio.{IO, ZIO}

import java.util.UUID

object TelegramCardCreateRequestMapper {
  def fromTelegram(request: TelegramCardCreateRequest, index: Int, spreadId: UUID): IO[TarotError, ExternalCard] = {
    for {
      _ <- ZIO.fail(ValidationError("index must not be positive")).when(index < 0)
      _ <- ZIO.fail(ValidationError("description must not be empty")).when(request.description.trim.isEmpty)
      _ <- ZIO.fail(ValidationError("coverPhotoId must not be empty")).when(request.coverPhotoId.trim.isEmpty)
    } yield toDomain(request, index, spreadId)
  }
  
  private def toDomain(request: TelegramCardCreateRequest, index: Int, spreadId: UUID): ExternalCard =
    ExternalCard(
      index = index,
      spreadId = SpreadId(spreadId),
      description = request.description,
      coverPhotoId = ExternalPhoto.Telegram(request.coverPhotoId))
}