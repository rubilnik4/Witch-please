package shared.models.api

sealed trait ApiError extends Throwable
object ApiError {
  final case class HttpCode(code: Int, description: String) extends ApiError
  final case class RequestFailed(message: String, cause: Throwable) extends ApiError
  final case class InvalidResponse(raw: String, reason: String) extends ApiError
}
