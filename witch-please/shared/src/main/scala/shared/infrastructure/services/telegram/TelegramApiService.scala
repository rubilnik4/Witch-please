package shared.infrastructure.services.telegram

import shared.api.dto.telegram.TelegramResponse
import shared.models.api.ApiError
import zio.ZIO

object TelegramApiService {
  def getTelegramResponse[T](response: TelegramResponse[T]): ZIO[Any, ApiError, T] =
    if (response.ok)
      ZIO.fromOption(response.result)
        .orElseFail(ApiError.InvalidResponse(response.toString, "Telegram result missing"))
    else
      ZIO.fail(ApiError.HttpCode(response.errorCode.getOrElse(500),
        response.description.getOrElse("Telegram unexpected error")))
}
