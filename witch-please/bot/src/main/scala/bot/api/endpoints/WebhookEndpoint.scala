package bot.api.endpoints

import bot.api.BotApiRoutes
import bot.api.dto.*
import bot.application.handlers.telegram.TelegramCommandHandlerLive
import bot.domain.models.telegram.TelegramMessage
import bot.layers.AppEnv
import sttp.model.StatusCode
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.ztapir.*
import zio.ZIO

object WebhookEndpoint {
  val postWebhookEndpoint: ZServerEndpoint[AppEnv, Any] =
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

          telegramCommandService <- ZIO.serviceWith[AppEnv](_.botCommandHandler.telegramCommandHandler)
          telegramMessage = TelegramMessage.fromRequest(request)
          _ <- telegramCommandService.handle(telegramMessage)
        } yield ())
          .catchAll(err => ZIO.logError(s"Webhook processing failed: $err").unit)
      }

  val endpoints: List[ZServerEndpoint[AppEnv, Any]] =
    List(postWebhookEndpoint)
}
