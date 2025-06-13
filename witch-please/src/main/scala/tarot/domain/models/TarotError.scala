package tarot.domain.models

import java.sql.SQLException

sealed trait TarotError

object TarotError {
  final case class NotFound(message: String) extends TarotError
  final case class DatabaseError(message: String, ex: SQLException) extends TarotError
  final case class CacheError(message: String, ex: Throwable) extends TarotError
  final case class ApiError(provider: String, code: Int, message: String) extends TarotError
  final case class ServiceUnavailable(service: String, ex: Throwable) extends TarotError
  final case class ValidationError(message: String) extends TarotError
  case object Unknown extends TarotError
}
