package tarot.fixtures

import shared.api.dto.tarot.photo.PhotoRequest
import shared.api.dto.tarot.spreads.{CardCreateRequest, SpreadCreateRequest, SpreadPublishRequest, SpreadUpdateRequest}
import shared.infrastructure.services.common.DateTimeService
import shared.models.files.FileSourceType
import tarot.layers.TarotEnv
import zio.ZIO
import zio.*

import java.time.Instant

object TarotTestRequests {
  def spreadCreateRequest(cardCount: Int, photoId: String): SpreadCreateRequest =
    SpreadCreateRequest(
      title = "Spread integration test",
      cardCount = cardCount,
      photo = PhotoRequest(FileSourceType.Telegram, photoId)
    )

  def spreadUpdateRequest(cardCount: Int, photoId: String): SpreadUpdateRequest =
    SpreadUpdateRequest(
      title = "Spread integration test",
      cardCount = cardCount,
      photo = PhotoRequest(FileSourceType.Telegram, photoId)
    )

  def cardCreateRequest(photoId: String): CardCreateRequest =
    CardCreateRequest(
      title = "Card integration test",
      photo = PhotoRequest(FileSourceType.Telegram, photoId)
    )

  def spreadPublishRequest(publishTime: Instant, cardOfDayDelayHours: Duration): SpreadPublishRequest =
    SpreadPublishRequest(publishTime, cardOfDayDelayHours)

  def spreadPublishRequest: ZIO[TarotEnv, Nothing, SpreadPublishRequest] =
    for {
      now <- DateTimeService.getDateTimeNow
      publishTime = now.plus(10.minute)
      cardOfDayDelayHours = 2.hours
    } yield SpreadPublishRequest(publishTime, cardOfDayDelayHours)
}
