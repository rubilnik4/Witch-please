package bot.infrastructure.services.telegram

import bot.api.BotApiRoutes
import bot.application.commands.telegram.TelegramCommands
import bot.layers.BotEnv
import zio.{Scope, ZIO, ZLayer}

object TelegramWebhookLaunch {
  val launch: ZLayer[BotEnv, Throwable, Unit] =
    ZLayer.scoped {
      for {
        telegramConfig <- ZIO.serviceWith[BotEnv](_.config.telegram)
        telegramWebhookService <- ZIO.serviceWith[BotEnv](_.services.telegramWebhookService)

        _ <- ZIO.logInfo(s"Launching telegram webhook")
        _ <- telegramWebhookService.setCommands(TelegramCommands.DefaultCommands)
        _ <- telegramWebhookService.setWebhook(s"${BotApiRoutes.apiPath}/webhook")
        info <- telegramWebhookService.getWebhookInfo
        _    <- ZIO.logInfo(s"Webhook info: ${info.url}")

        _ <- Scope.addFinalizer(telegramWebhookService.deleteWebhook().ignore)
      } yield ()
    }
}
