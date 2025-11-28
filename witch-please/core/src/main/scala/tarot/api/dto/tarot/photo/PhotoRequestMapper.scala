package tarot.api.dto.tarot.photo

import shared.api.dto.tarot.photo.{PhotoRequest, PhotoResponse}
import shared.api.dto.tarot.spreads.{SpreadCreateRequest, SpreadRequest}
import tarot.application.commands.spreads.commands.CreateSpreadCommand
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.ValidationError
import tarot.domain.models.photo.{PhotoFile, Photo}
import zio.{IO, ZIO}

object PhotoRequestMapper {
  def fromRequest(request: PhotoRequest): IO[TarotError, PhotoFile] =
    validate(request).as(toDomain(request))

  private def toDomain(request: PhotoRequest): PhotoFile =
    PhotoFile(
      sourceType = request.sourceType,
      fileId = request.fileId,
    )

  private def validate(request: PhotoRequest) =
    for {
      _ <- ZIO.fail(ValidationError("photo fileId must not be empty")).when(request.fileId.trim.isEmpty)
    } yield ()
}