package shared.api.dto.tarot.errors

import zio.*
import zio.json.*
import sttp.tapir.Schema
import zio.json.jsonDiscriminator

@jsonDiscriminator("error")
enum TarotErrorResponse derives JsonCodec, Schema:
  case NotFoundError(message: String)
  case BadRequestError(message: String)
  case ConflictError(message: String)
  case Unauthorized(message: String)
  case InternalServerError(message: String, cause: Option[String] = None)

//object TarotErrorResponse:
//  given JsonCodec[BadRequestError] = DeriveJsonCodec.gen
//  given JsonCodec[InternalServerError] = DeriveJsonCodec.gen
//  given JsonCodec[NotFoundError] = DeriveJsonCodec.gen
//  given JsonCodec[ConflictError] = DeriveJsonCodec.gen
//  given JsonCodec[Unauthorized] = DeriveJsonCodec.gen
//
//  given Schema[BadRequestError] = Schema.derived
//  given Schema[InternalServerError] = Schema.derived
//  given Schema[NotFoundError] = Schema.derived
//  given Schema[ConflictError] = Schema.derived
//  given Schema[Unauthorized] = Schema.derived
