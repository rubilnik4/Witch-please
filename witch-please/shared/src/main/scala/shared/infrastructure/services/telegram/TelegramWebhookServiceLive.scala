package shared.infrastructure.services.telegram

import shared.api.dto.telegram.*
import shared.application.configurations.TelegramConfig
import shared.infrastructure.services.clients.SttpClient
import shared.models.api.ApiError
import sttp.client3.*
import sttp.client3.ziojson.asJsonEither
import zio.*
import zio.json.*

final class TelegramWebhookServiceLive(config: TelegramConfig, client: SttpBackend[Task, Any])
  extends TelegramWebhookService {

  private final val baseUrl = s"https://api.telegram.org/bot${config.token}"
  private final val getWebhookUrl = uri"$baseUrl/getWebhookInfo"
  private final val setWebhookUrl = uri"$baseUrl/setWebhook"
  private final val deleteWebhookUrl = uri"$baseUrl/deleteWebhook"
  private final val maxConnections = 40

  def setWebhook(webhookPath: String): ZIO[Any, ApiError, Unit] =
    for {
      _ <- ZIO.logInfo(s"Setting webhook $webhookPath")
      request = setWebhookRequest(webhookPath)
      response <- SttpClient.sendJson(client, request)
      _ <- TelegramApiService.getTelegramResponse[Boolean](response)
    } yield ()

  def deleteWebhook(): ZIO[Any, ApiError, Unit] =
    for {
      _ <- ZIO.logInfo(s"Deleting webhook")
      request = deleteWebhookRequest()
      response <- SttpClient.sendJson(client, request)
      _ <- TelegramApiService.getTelegramResponse[Boolean](response)
    } yield ()

  def getWebhookInfo: ZIO[Any, ApiError, TelegramWebhookInfo] =
    for {
      _ <- ZIO.logInfo(s"Getting webhook info")
      request = getWebhookRequest
      response <- SttpClient.sendJson(client, request)
      webhookInfo <- TelegramApiService.getTelegramResponse[TelegramWebhookInfo](response)
    } yield webhookInfo

  private def getWebhookRequest = {
    SttpClient
      .getRequest(getWebhookUrl)
      .response(asJsonEither[TelegramErrorResponse, TelegramResponse[TelegramWebhookInfo]])
  }

  private def setWebhookRequest(webhookPath: String) = {
    val request = TelegramSetWebhookRequest(
      url = s"${config.baseUrl}$webhookPath",
      secretToken = config.secret,
      maxConnections = maxConnections,
      dropPendingUpdates = true,
      allowedUpdates = None
    )
    SttpClient.postRequest(setWebhookUrl, request)
      .response(asJsonEither[TelegramErrorResponse, TelegramResponse[Boolean]])
  }

  private def deleteWebhookRequest() = {
    val body = TelegramDeleteWebhookRequest(
      dropPendingUpdates = true
    )
    SttpClient
      .postRequest(deleteWebhookUrl, body)
      .response(asJsonEither[TelegramErrorResponse, TelegramResponse[Boolean]])
  }
}
