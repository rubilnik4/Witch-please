package tarot.api.dto.tarot.photo

import shared.api.dto.tarot.photo.{PhotoRequest, PhotoResponse}
import shared.api.dto.tarot.spreads.{SpreadCreateRequest, SpreadRequest}
import tarot.application.commands.spreads.commands.CreateSpreadCommand
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.ValidationError
import tarot.domain.models.photo.{PhotoSource, Photo}
import zio.{IO, ZIO}

object PhotoRequestMapper {
  def fromRequest(request: PhotoRequest): IO[TarotError, PhotoSource] =
    validate(request).as(toDomain(request))

  private def toDomain(request: PhotoRequest): PhotoSource =
    PhotoSource(
      sourceType = request.sourceType,
      sourceId = request.sourceId,
    )

  private def validate(request: PhotoRequest) =
    for {
      _ <- ZIO.fail(ValidationError("photo fileId must not be empty")).when(request.sourceId.trim.isEmpty)
    } yield ()
}