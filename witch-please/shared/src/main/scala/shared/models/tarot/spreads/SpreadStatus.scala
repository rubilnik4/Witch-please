package shared.models.tarot.spreads

import sttp.tapir.Schema
import zio.json.JsonCodec

enum SpreadStatus derives JsonCodec, Schema {
  case Draft
  case Ready
  case Published
  case Archived
}
