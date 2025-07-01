package tarot.api.dto

import tarot.domain.models.TarotError
import TarotError.ValidationError
import tarot.domain.models.photo.{ExternalPhotoSource, PhotoSource}
import tarot.domain.models.spreads.{ExternalSpread, Spread, SpreadStatus}
import zio.{IO, ZIO}
import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.schema.{DeriveSchema, Schema}
import zio.json.*
import zio.schema.*

import java.util.UUID

final case class TelegramSpreadRequest(
  title: String, 
  cardCount: Int, 
  coverPhotoId: String
) derives JsonCodec, Schema

object TelegramSpreadRequest {
  def fromTelegram(request: TelegramSpreadRequest): IO[TarotError, ExternalSpread] = {
    for {
      _ <- ZIO.fail(ValidationError("title must not be empty")).when(request.title.trim.isEmpty)
      _ <- ZIO.fail(ValidationError("cardCount must be > 0")).when(request.cardCount <= 0)
      _ <- ZIO.fail(ValidationError("coverPhotoId must not be empty")).when(request.coverPhotoId.trim.isEmpty)
    } yield toDomain(request)
  }
  
  private def toDomain(request: TelegramSpreadRequest): ExternalSpread =
    ExternalSpread(
      title = request.title,
      cardCount = request.cardCount,
      coverPhotoId = ExternalPhotoSource.Telegram(request.coverPhotoId))
}