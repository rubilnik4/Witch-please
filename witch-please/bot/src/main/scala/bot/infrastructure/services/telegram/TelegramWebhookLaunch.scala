package bot.infrastructure.services.telegram

import bot.api.BotApiRoutes
import bot.application.commands.telegram.TelegramCommands
import bot.layers.BotEnv
import zio.{Scope, ZIO, ZLayer}

object TelegramWebhookLaunch {
  val launch: ZLayer[BotEnv, Nothing, Unit] =
    ZLayer.scoped {
      for {
        _ <- launchWebhook()
      } yield ()
    }

  private def launchWebhook() =
    (for {
      telegramWebhookService <- ZIO.serviceWith[BotEnv](_.services.telegramWebhookService)

      _ <- ZIO.logInfo(s"Launching telegram webhook")
      _ <- telegramWebhookService.setCommands(TelegramCommands.DefaultCommands)
      _ <- telegramWebhookService.setWebhook(s"${BotApiRoutes.apiPath}/webhook")
      _ <- Scope.addFinalizer(telegramWebhookService.deleteWebhook().ignore)

      info <- telegramWebhookService.getWebhookInfo
      _ <- ZIO.logInfo(s"Webhook info: ${info.url}")
    } yield ()).catchAll { error =>
      ZIO.logError(s"Telegram webhook launch failed, bot HTTP server will continue without webhook initialization: $error")
    }
}
