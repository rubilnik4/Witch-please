package tarot.domain.models

import shared.models.api.ApiError

sealed trait TarotError

object TarotError {
  final case class NotFound(message: String) extends TarotError
  final case class DatabaseError(message: String, ex: Throwable) extends TarotError
  final case class CacheError(message: String, ex: Throwable) extends TarotError
  final case class StorageError(message: String, ex: Throwable) extends TarotError
  final case class ApiError(code: Int, message: String) extends TarotError
  final case class ServiceUnavailable(message: String, ex: Throwable) extends TarotError
  final case class ValidationError(message: String) extends TarotError
  final case class SerializationError(message: String) extends TarotError
  final case class ParsingError(raw: String, message: String) extends TarotError
  final case class UnsupportedType(message: String) extends TarotError
  final case class Conflict(message: String) extends TarotError
  final case class Unauthorized(message: String) extends TarotError
  case object Unknown extends TarotError
}

object TarotErrorMapper {
  def toTarotError(error: ApiError): TarotError = error match {
    case ApiError.HttpCode(code, description) =>
      TarotError.ApiError(code, description)
    case ApiError.RequestFailed(message, cause) =>
      TarotError.ServiceUnavailable(message, cause)
    case ApiError.InvalidResponse(_, reason) =>
      TarotError.ApiError(500, reason)
  }
}
