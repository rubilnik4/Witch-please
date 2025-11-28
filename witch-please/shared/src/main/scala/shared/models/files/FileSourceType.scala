package shared.models.files

import sttp.tapir.Schema
import zio.json.JsonCodec

enum FileSourceType derives JsonCodec, Schema {
  case Telegram
  case S3
}
