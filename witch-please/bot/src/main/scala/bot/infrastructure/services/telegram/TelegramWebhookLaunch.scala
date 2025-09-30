package bot.infrastructure.services.telegram

import bot.api.BotApiRoutes
import bot.layers.BotEnv
import shared.models.api.ApiError
import zio.{Scope, ZIO, ZLayer}

object TelegramWebhookLaunch {
  val launch: ZLayer[BotEnv, Throwable, Unit] =
    ZLayer.scoped {
      for {
        telegramConfig <- ZIO.serviceWith[BotEnv](_.config.telegram)
        telegramWebhookService <- ZIO.serviceWith[BotEnv](_.botService.telegramWebhookService)
        _ <- telegramWebhookService.setWebhook(s"${BotApiRoutes.apiPath}/webhook")
        info <- telegramWebhookService.getWebhookInfo
        _ <- Scope.addFinalizer(telegramWebhookService.deleteWebhook().ignore)
      } yield ()
    }
}
