package bot.infrastructure.services

import bot.api.dto.*
import common.api.dto.telegram.TelegramMessageRequest
import common.infrastructure.services.clients.ZIOHttpClient
import zio.*
import zio.http.URL

final case class TelegramApiServiceLive(botToken: String) extends TelegramApiService {
  private val baseUrl = s"https://api.telegram.org/bot$botToken"

  override def sendText(chatId: Long, text: String): ZIO[AppEnv, Throwable, Unit] = {
    val url = URL.decode(s"$baseUrl/sendMessage").toOption.get
    val requestBody = TelegramMessageRequest(chatId, text)

    for {
      _ <- ZIO.logDebug(s"Sending text to Telegram: chatId=$chatId, text=$text")
      request = ZIOHttpClient.getPostRequest(url, requestBody)
      response <- Client.request(request)
      _ <- ZIO.when(!response.status.isSuccess)(ZIO.logWarning(s"Telegram API error: ${response.status}"))
    } yield ()
  }

  override def sendPhoto(chatId: Long, fileId: String): Task[Unit] = {
    val url = URL.decode(s"$baseUrl/sendPhoto").toOption.get
    val payload = Map("chat_id" -> chatId.toString, "photo" -> fileId).toJson

    val request = Request.post(url, Body.fromString(payload))
      .addHeader("Content-Type", "application/json")

    Client.request(request).unit
  }
}

