package tarot.api.dto

import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.ValidationError
import tarot.domain.models.photo.PhotoLocation
import tarot.domain.models.spreads.{ExternalSpread, Spread, SpreadStatus}
import zio.{IO, ZIO}
import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.schema.{DeriveSchema, Schema}

import java.util.UUID

final case class TelegramSpreadRequest(title: String, cardCount: Int, coverPhotoId: String)

object TelegramSpreadRequest {
  given JsonCodec[TelegramSpreadRequest] = DeriveJsonCodec.gen
  given Schema[TelegramSpreadRequest] = DeriveSchema.gen

  def validate(req: TelegramSpreadRequest): IO[TarotError, TelegramSpreadRequest] = {
    for {
      _ <- ZIO.fail(ValidationError("title must not be empty")).when(req.title.trim.isEmpty)
      _ <- ZIO.fail(ValidationError("cardCount must be > 0")).when(req.cardCount <= 0)
      _ <- ZIO.fail(ValidationError("coverPhotoId must not be empty")).when(req.coverPhotoId.trim.isEmpty)
    } yield req
  }
}

object TelegramSpread {
  def fromTelegram(request: TelegramSpreadRequest): ExternalSpread =
    ExternalSpread(
      title = request.title,
      cardCount = request.cardCount,
      coverPhotoId = PhotoLocation.Telegram(request.coverPhotoId))
}