package shared.models.tarot.cardOfDay

import sttp.tapir.Schema
import zio.json.JsonCodec

enum CardOfDayStatus derives JsonCodec, Schema {
  case Draft
  case Scheduled
  case Published
}
