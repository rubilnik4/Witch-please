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
