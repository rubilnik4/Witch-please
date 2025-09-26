package shared.infrastructure.services.telegram

import shared.api.dto.telegram.TelegramWebhookInfo
import shared.models.api.ApiError
import zio.ZIO

trait TelegramWebhookService {
  def setWebhook(webhookPath: String): ZIO[Any, ApiError, Unit]
  def deleteWebhook(): ZIO[Any, ApiError, Unit]
  def getWebhookInfo: ZIO[Any, ApiError, TelegramWebhookInfo]
}
