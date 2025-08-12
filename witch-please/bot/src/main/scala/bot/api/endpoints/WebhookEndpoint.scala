package bot.api.endpoints

import bot.api.dto.*
import sttp.model.StatusCode
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.ztapir.*
import zio.ZIO

object WebhookEndpoint {
  val postWebhookEndpoint: ZServerEndpoint[AppEnv, Any] =
    endpoint.post
      .in("webhook")
      .in(jsonBody[TelegramWebhookRequest])
      .out(statusCode(StatusCode.Ok))
      .zServerLogic { request =>
        (for {
          _ <- ZIO.logInfo(s"Received webhook from Telegram for user: ${request.name}")

          handler <- ZIO.serviceWith[AppEnv](_.tarotCommandHandler.userCreateCommandHandler)
          command = UserCreateCommand(externalUser)
          _ <- handler.handle(command)

        } yield ())
          .catchAll { err =>
            ZIO.logError(s"Webhook processing failed: $err").unit
          }
      }
}
