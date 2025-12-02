package bot.api.endpoints

import bot.api.BotApiRoutes
import bot.api.dto.*
import bot.domain.models.telegram.TelegramMessage
import bot.layers.BotEnv
import sttp.model.StatusCode
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.ztapir.*
import zio.ZIO

object WebhookEndpoint {
  private val postWebhookEndpoint: ZServerEndpoint[BotEnv, Any] =
    endpoint.post
      .in(BotApiRoutes.apiPath / "webhook")
      .in(jsonBody[TelegramWebhookRequest])
      .out(statusCode(StatusCode.Ok))
      .zServerLogic { request =>
        val chatId = request.message.map(_.chat.id)
        val userId = request.message.flatMap(_.from.map(_.id))
        val username = request.message.flatMap(_.from.map(_.username)).getOrElse("unknown")
        val text = request.message.flatMap(_.text)
        (for {
          _ <- ZIO.logInfo(s"Telegram webhook: chat_id=$chatId, user_id=$username, user=$username, text=$text")

          telegramCommandService <- ZIO.serviceWith[BotEnv](_.commandHandlers.telegramCommandHandler)
          telegramMessage = TelegramMessage.fromRequest(request)
          _ <- telegramCommandService.handle(telegramMessage)
        } yield ())
          .catchAll(err => ZIO.logError(s"Webhook processing failed: $err").unit)
      }

  val endpoints: List[ZServerEndpoint[BotEnv, Any]] =
    List(postWebhookEndpoint)
}
