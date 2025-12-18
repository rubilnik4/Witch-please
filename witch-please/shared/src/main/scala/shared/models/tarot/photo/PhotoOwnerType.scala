package shared.models.tarot.photo

import zio.json._
import sttp.tapir.Schema

enum PhotoOwnerType derives JsonCodec, Schema:
  case Spread, Card, CardOfDay
