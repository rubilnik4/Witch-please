package tarot.api.dto.tarot

import tarot.domain.models.TarotError
import zio.*
import zio.json.*
import zio.schema.{DeriveSchema, Schema}

sealed trait TarotErrorResponse

object TarotErrorResponse {
  case class NotFoundError(message: String) extends TarotErrorResponse

  case class BadRequestError(message: String) extends TarotErrorResponse

  case class ConflictError(message: String) extends TarotErrorResponse

  case class Unauthorized(message: String) extends TarotErrorResponse

  case class InternalServerError(message: String, cause: Option[String] = None)
    extends TarotErrorResponse

  given JsonCodec[TarotErrorResponse] = DeriveJsonCodec.gen
  given Schema[TarotErrorResponse] = DeriveSchema.gen
}

object TarotErrorMapper {
  def toResponse(error: TarotError): TarotErrorResponse = error match {
    case TarotError.NotFound(msg) =>
      TarotErrorResponse.NotFoundError(msg)
    case TarotError.DatabaseError(msg, ex) =>
      TarotErrorResponse.InternalServerError(s"Database error: $msg", Some(ex.getMessage))
    case TarotError.CacheError(msg, ex) =>
      TarotErrorResponse.InternalServerError(s"Cache error: $msg", Some(ex.getMessage))
    case TarotError.ApiError(provider, code, msg) =>
      TarotErrorResponse.InternalServerError(s"External API error from $provider with code $code: $msg",
        Some("External API call failed"))
    case TarotError.ServiceUnavailable(service, ex) =>
      TarotErrorResponse.InternalServerError(s"Service '$service' is currently unavailable", Some(ex.getMessage))
    case TarotError.Unknown =>
      TarotErrorResponse.InternalServerError("Unknown error occurred", Some("No additional info"))
    case TarotError.StorageError(msg, ex) =>
      TarotErrorResponse.InternalServerError(s"Storage error: $msg",Some(ex.getMessage))
    case TarotError.ValidationError(msg) =>
      TarotErrorResponse.BadRequestError(s"Validation failed: $msg")
    case TarotError.SerializationError(msg) =>
      TarotErrorResponse.InternalServerError(s"Serialization failed", Some(msg))
    case TarotError.ParsingError(path, msg) =>
      TarotErrorResponse.InternalServerError(s"Parsing failed at '$path': $msg")
    case TarotError.Conflict(msg) =>
      TarotErrorResponse.ConflictError(s"Conflict error: $msg")
    case TarotError.Unauthorized(msg) =>
      TarotErrorResponse.Unauthorized(s"Unauthorize: $msg")
  }
}
