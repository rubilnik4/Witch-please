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
  private final val setCommandsUrl = uri"$baseUrl/setMyCommands"
  private final val maxConnections = 40

  def setWebhook(webhookPath: String): ZIO[Any, ApiError, Unit] =
    for {
      _ <- ZIO.logInfo(s"Setting telegram webhook $webhookPath")
      request = setWebhookRequest(webhookPath)
      response <- SttpClient.sendJson(client, request)
      _ <- TelegramApi.getTelegramResponse[Boolean](response)
    } yield ()

  def deleteWebhook(): ZIO[Any, ApiError, Unit] =
    for {
      _ <- ZIO.logInfo(s"Deleting telegram webhook")
      request = deleteWebhookRequest()
      response <- SttpClient.sendJson(client, request)
      _ <- TelegramApi.getTelegramResponse[Boolean](response)
    } yield ()

  def getWebhookInfo: ZIO[Any, ApiError, TelegramWebhookInfo] =
    for {
      _ <- ZIO.logDebug(s"Getting telegram webhook info")
      request = getWebhookRequest
      response <- SttpClient.sendJson(client, request)
      webhookInfo <- TelegramApi.getTelegramResponse[TelegramWebhookInfo](response)
    } yield webhookInfo

  def setCommands(commands: List[TelegramCommandRequest]): ZIO[Any, ApiError, Unit] =
    for {
      _ <- ZIO.logInfo(s"Setting telegram commands")
      request = setCommandsRequest(commands)
      response <- SttpClient.sendJson(client, request)
      _ <- TelegramApi.getTelegramResponse[Boolean](response)
    } yield ()

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
    val body = TelegramDeleteWebhookRequest(dropPendingUpdates = true)
    SttpClient
      .postRequest(deleteWebhookUrl, body)
      .response(asJsonEither[TelegramErrorResponse, TelegramResponse[Boolean]])
  }

  private def setCommandsRequest(commands: List[TelegramCommandRequest]) = {
    val request = TelegramSetCommandsRequest(commands)
    SttpClient.postRequest(setCommandsUrl, request)
      .response(asJsonEither[TelegramErrorResponse, TelegramResponse[Boolean]])
  }
}
