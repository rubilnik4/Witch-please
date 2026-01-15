package tarot.fixtures

import shared.api.dto.tarot.cardsOfDay.*
import shared.api.dto.tarot.cards.*
import shared.api.dto.tarot.photo.PhotoRequest
import shared.api.dto.tarot.spreads.*
import shared.infrastructure.services.common.DateTimeService
import shared.models.files.FileSourceType
import tarot.layers.TarotEnv
import zio.ZIO
import zio.*

import java.time.Instant
import java.util.UUID

object TarotTestRequests {
  def spreadCreateRequest(cardCount: Int, photoId: String): SpreadCreateRequest =
    SpreadCreateRequest(
      title = "Spread integration test",
      cardCount = cardCount,
      description = "Spread integration test",
      photo = PhotoRequest(FileSourceType.Telegram, photoId)
    )

  def spreadUpdateRequest(cardCount: Int, photoId: String): SpreadUpdateRequest =
    SpreadUpdateRequest(
      title = "Spread integration test",
      cardCount = cardCount,
      description = "Spread integration test",
      photo = PhotoRequest(FileSourceType.Telegram, photoId)
    )

  def cardCreateRequest(position: Int, photoId: String): CardCreateRequest =
    CardCreateRequest(
      position = position,
      title = "Card integration test",
      description =  "Card integration test",
      photo = PhotoRequest(FileSourceType.Telegram, photoId)
    )

  def cardUpdateRequest(photoId: String): CardUpdateRequest =
    CardUpdateRequest(
      title = "Card integration test",
      description =  "Card integration test",
      photo = PhotoRequest(FileSourceType.Telegram, photoId)
    )

  def spreadPublishRequest: ZIO[TarotEnv, Nothing, SpreadPublishRequest] =
    for {
      now <- DateTimeService.getDateTimeNow
      publishTime = now.plus(10.minute)
      cardOfDayDelayHours = 2.hours
    } yield spreadPublishRequest(publishTime, cardOfDayDelayHours)

  def spreadPublishRequest(publishTime: Instant, cardOfDayDelayHours: Duration): SpreadPublishRequest =
    SpreadPublishRequest(
      scheduledAt = publishTime,
      cardOfDayDelayHours = cardOfDayDelayHours
    )

  def cardOfDayCreateRequest(cardId: UUID, photoId: String): CardOfDayCreateRequest =
    CardOfDayCreateRequest(
      cardId = cardId,     
      description = "Card of day integration test",
      photo = PhotoRequest(FileSourceType.Telegram, photoId)
    )

  def cardOfDayUpdateRequest(cardId: UUID, photoId: String): CardOfDayUpdateRequest =
    CardOfDayUpdateRequest(     
      cardId = cardId,
      description = "Card of day integration test",
      photo = PhotoRequest(FileSourceType.Telegram, photoId)
    )

}
