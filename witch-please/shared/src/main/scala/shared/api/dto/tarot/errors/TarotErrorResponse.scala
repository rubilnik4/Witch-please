package shared.api.dto.tarot.errors

import zio.*
import zio.json.*
import zio.schema.{DeriveSchema, Schema}

sealed trait TarotErrorResponse

object TarotErrorResponse {
  case class NotFoundError(message: String) extends TarotErrorResponse
  case class BadRequestError(message: String) extends TarotErrorResponse
  case class ConflictError(message: String) extends TarotErrorResponse
  case class Unauthorized(message: String) extends TarotErrorResponse
  case class InternalServerError(message: String, cause: Option[String] = None) extends TarotErrorResponse

  given JsonCodec[TarotErrorResponse] = DeriveJsonCodec.gen
  given Schema[TarotErrorResponse] = DeriveSchema.gen
}
