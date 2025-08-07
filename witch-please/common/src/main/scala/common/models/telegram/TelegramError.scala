package common.models.telegram

sealed trait TelegramError extends Throwable
object TelegramError {
  final case class ApiError(code: Int, description: String) extends TelegramError
  final case class RequestFailed(message: String, cause: Throwable) extends TelegramError
  final case class InvalidResponse(raw: String, reason: String) extends TelegramError
}
