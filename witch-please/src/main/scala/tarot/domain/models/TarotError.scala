package tarot.domain.models


sealed trait TarotError

object TarotError {
  final case class NotFound(message: String) extends TarotError
  final case class DatabaseError(message: String, ex: Throwable) extends TarotError
  final case class CacheError(message: String, ex: Throwable) extends TarotError
  final case class StorageError(message: String, ex: Throwable) extends TarotError
  final case class ApiError(provider: String, code: Int, message: String) extends TarotError
  final case class ServiceUnavailable(service: String, ex: Throwable) extends TarotError
  final case class ValidationError(message: String) extends TarotError
  final case class SerializationError(message: String) extends TarotError
  final case class ParsingError(raw: String, message: String) extends TarotError
  final case class Conflict(message: String) extends TarotError
  final case class Unauthorized(message: String) extends TarotError
  case object Unknown extends TarotError
}
