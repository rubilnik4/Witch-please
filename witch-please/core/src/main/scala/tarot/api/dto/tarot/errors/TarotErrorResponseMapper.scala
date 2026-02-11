package tarot.api.dto.tarot.errors

import shared.api.dto.tarot.errors.*
import tarot.domain.models.TarotError
import zio.*
import zio.http.{Response, Status}
import zio.json.*

object TarotErrorResponseMapper {
  def toResponse(error: TarotError): TarotErrorResponse = error match {
    case TarotError.NotFound(msg) =>
      TarotErrorResponse.NotFoundError(msg)
    case TarotError.DatabaseError(msg, ex) =>
      TarotErrorResponse.InternalServerError(s"Database error: $msg", Some(ex.getMessage))
    case TarotError.CacheError(msg, ex) =>
      TarotErrorResponse.InternalServerError(s"Cache error: $msg", Some(ex.getMessage))
    case TarotError.ApiError(code, msg) =>
      TarotErrorResponse.InternalServerError(s"External API error with code $code: $msg",
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
    case TarotError.UnsupportedType(msg) =>
      TarotErrorResponse.InternalServerError(s"Unsupported type error: $msg")
    case TarotError.Conflict(msg) =>
      TarotErrorResponse.ConflictError(s"Conflict error: $msg")
    case TarotError.Unauthorized(msg) =>
      TarotErrorResponse.Unauthorized(s"Unauthorize: $msg")
  }

  def toHttpResponse(error: TarotError): Response = {
    val errorResponse = toResponse(error)
    val json = errorResponse.toJson
    val status = errorResponse match {
      case _: TarotErrorResponse.NotFoundError => Status.NotFound
      case _: TarotErrorResponse.BadRequestError => Status.BadRequest
      case _: TarotErrorResponse.ConflictError => Status.Conflict
      case _: TarotErrorResponse.Unauthorized => Status.Unauthorized
      case _: TarotErrorResponse.InternalServerError => Status.InternalServerError
    }
    Response.json(json).status(status)
  }
}
