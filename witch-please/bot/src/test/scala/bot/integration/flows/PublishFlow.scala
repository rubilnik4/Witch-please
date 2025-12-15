package bot.integration.flows

import bot.api.BotApiRoutes
import bot.layers.BotEnv
import bot.telegram.TestTelegramWebhook
import shared.infrastructure.services.clients.ZIOHttpClient
import shared.infrastructure.services.common.DateTimeService
import zio.http.{Response, Routes}
import zio.{Scope, ZIO}

import java.time.{Duration, Instant}
import java.util.UUID

object PublishFlow {
  def startPublish(app: Routes[BotEnv, Response], chatId: Long, spreadId: UUID): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.publishSpreadRequest(chatId, spreadId)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)
    } yield ()

  def selectMonth(app: Routes[BotEnv, Response], chatId: Long, scheduleTime: Instant): ZIO[Scope & BotEnv, Throwable, Unit] =
    val month = DateTimeService.getYearMonth(scheduleTime)
    val postRequest = TestTelegramWebhook.scheduleSelectMonth(chatId, month)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)
    } yield ()

  def selectDate(app: Routes[BotEnv, Response], chatId: Long, scheduleTime: Instant): ZIO[Scope & BotEnv, Throwable, Unit] =
    val date = DateTimeService.getLocalDate(scheduleTime)
    val postRequest = TestTelegramWebhook.scheduleSelectDate(chatId, date)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)

      botSessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
      session <- botSessionService.get(chatId)
      _ <- ZIO.fromOption(session.date).orElseFail(new RuntimeException(s"date is empty, expected $scheduleTime"))
    } yield ()

  def selectTime(app: Routes[BotEnv, Response], chatId: Long, scheduleTime: Instant): ZIO[Scope & BotEnv, Throwable, Unit] =
    val time = DateTimeService.getLocalTime(scheduleTime)
    val postRequest = TestTelegramWebhook.scheduleSelectTime(chatId, time.toLocalTime)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)

      botSessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
      session <- botSessionService.get(chatId)
      _ <- ZIO.fromOption(session.time).orElseFail(new RuntimeException(s"time is empty, expected $time"))
    } yield ()

  def selectCardOdDayDelay(app: Routes[BotEnv, Response], chatId: Long, cardOfDayDelay: Duration): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.scheduleSelectCardOfDayDelay(chatId, cardOfDayDelay)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)

      botSessionService <- ZIO.serviceWith[BotEnv](_.services.botSessionService)
      session <- botSessionService.get(chatId)
      _ <- ZIO.fromOption(session.cardOfDayDelay).orElseFail(new RuntimeException(s"cardOfDayDelay is empty, expected $cardOfDayDelay"))
    } yield ()

  def confirm(app: Routes[BotEnv, Response], chatId: Long): ZIO[Scope & BotEnv, Throwable, Unit] =
    val postRequest = TestTelegramWebhook.scheduleConfirm(chatId)
    val request = ZIOHttpClient.postRequest(BotApiRoutes.postWebhookPath(""), postRequest)
    for {
      _ <- app.runZIO(request)
    } yield ()
}
