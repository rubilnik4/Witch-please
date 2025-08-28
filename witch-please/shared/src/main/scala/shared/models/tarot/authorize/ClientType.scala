package shared.models.tarot.authorize

import zio.json._
import sttp.tapir.Schema

enum ClientType derives JsonCodec, Schema:
  case Telegram, Web, MobileApp