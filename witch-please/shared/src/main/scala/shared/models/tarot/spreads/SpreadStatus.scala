package shared.models.tarot.spreads

import sttp.tapir.Schema
import zio.json.JsonCodec

enum SpreadStatus derives JsonCodec, Schema {
  case Draft
  case Scheduled
  case Published
  case Archived
}
