package tarot.api.dto.tarot.spreads

import shared.api.dto.tarot.spreads.*
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.ValidationError
import tarot.domain.models.photo.ExternalPhoto
import tarot.domain.models.projects.ProjectId
import tarot.domain.models.spreads.ExternalSpread
import zio.json.*
import zio.schema.*
import zio.{IO, ZIO}

import java.util.UUID

object TelegramSpreadCreateRequestMapper {
  def fromTelegram(request: TelegramSpreadCreateRequest): IO[TarotError, ExternalSpread] = {
    for {
      _ <- ZIO.fail(ValidationError("title must not be empty")).when(request.title.trim.isEmpty)
      _ <- ZIO.fail(ValidationError("cardCount must be > 0")).when(request.cardCount <= 0)
      _ <- ZIO.fail(ValidationError("coverPhotoId must not be empty")).when(request.coverPhotoId.trim.isEmpty)
    } yield toDomain(request)
  }
  
  private def toDomain(request: TelegramSpreadCreateRequest): ExternalSpread =
    ExternalSpread(
      projectId = ProjectId(request.projectId),
      title = request.title,
      cardCount = request.cardCount,
      coverPhotoId = ExternalPhoto.Telegram(request.coverPhotoId))
}