package tarot.infrastructure.services.telegram

import shared.models.files.FileSourceType
import tarot.domain.models.cards.Card
import tarot.domain.models.cardsOfDay.CardOfDay
import tarot.domain.models.photo.Photo
import tarot.domain.models.{TarotError, TarotErrorMapper}
import tarot.domain.models.spreads.Spread
import tarot.layers.TarotEnv
import zio.ZIO

final class TelegramPublishServiceLive extends TelegramPublishService:
  override def publishSpread(chatId: Long, spread: Spread, cards: List[Card]): ZIO[TarotEnv, TarotError, Unit] =
    for {
      _ <- ZIO.logDebug(s"Publish spread ${spread.id} to telegram chat $chatId")

      telegramApiService <- ZIO.serviceWith[TarotEnv](_.services.telegramApiService)

      text = buildSpreadText(spread, cards)
      _ <- telegramApiService.sendText(chatId, text)
        .mapError(err => TarotErrorMapper.toTarotError("TelegramPublishServiceLive", err))

      caption = s"🖼️ Расклад: ${spread.title}"
      photos = spread.photo :: cards.map(_.photo)
      fileIds <- ZIO.foreach(photos)(getTelegramPhotoId)
      _ <- telegramApiService.sendPhotos(chatId, caption, fileIds)
        .mapError(error => TarotErrorMapper.toTarotError("TelegramPublishServiceLive", error))
    } yield ()

  override def publishCardOfDay(chatId: Long, cardOfDay: CardOfDay): ZIO[TarotEnv, TarotError, Unit] =
    for {
      _ <- ZIO.logDebug(s"Publish card of day ${cardOfDay.id} to telegram chat $chatId")

      telegramApiService <- ZIO.serviceWith[TarotEnv](_.services.telegramApiService)

      text = buildCardOfDayText(cardOfDay)
      _ <- telegramApiService.sendText(chatId,text)
        .mapError(err => TarotErrorMapper.toTarotError("TelegramPublishServiceLive", err))

      caption = s"🃏 Карта дня: ${cardOfDay.title}"
      fileId <- getTelegramPhotoId(cardOfDay.photo)      
      _ <- telegramApiService.sendPhotos(chatId, caption, List(fileId))
        .mapError(error => TarotErrorMapper.toTarotError("TelegramPublishServiceLive", error))
    } yield ()
    
  private def getTelegramPhotoId(photo: Photo) =
    photo.sourceType match {
      case FileSourceType.Telegram =>
        ZIO.succeed(photo.sourceId)
      case other =>
        ZIO.fail(TarotError.ParsingError(other.toString, s"Unsupported photo ${photo.id} source for telegram publish: $other"))
    }

  private def buildSpreadText(spread: Spread, cards: List[Card]): String = {
    val header =
      s"""🃏 Расклад: "${spread.title}"
         |Карт: ${spread.cardsCount}
         |
         |${normalize(spread.description)}
         |""".stripMargin.trim

    val cardsBlock =
      cards
        .sortBy(_.position)
        .map { c =>
          s"""${numberEmoji(c.position + 1)} ${c.title}
             |${normalize(c.description)}
             |""".stripMargin.trim
        }
        .mkString("\n\n")

    s"$header\n\n$cardsBlock".trim
  }

  private def buildCardOfDayText(card: CardOfDay): String =
    s"""🌙 Карта дня: ${card.title}
       |
       |${normalize(card.description)}
       |""".stripMargin.trim

  private def normalize(s: String): String =
    s.trim.replace("\r\n", "\n").replaceAll("\n{3,}", "\n\n")

  private def numberEmoji(n: Int): String =
    n match {
      case 1 => "1️⃣"
      case 2 => "2️⃣"
      case 3 => "3️⃣"
      case 4 => "4️⃣"
      case 5 => "5️⃣"
      case 6 => "6️⃣"
      case 7 => "7️⃣"
      case 8 => "8️⃣"
      case 9 => "9️⃣"
      case 10 => "🔟"
      case _ => s"$n."
    }