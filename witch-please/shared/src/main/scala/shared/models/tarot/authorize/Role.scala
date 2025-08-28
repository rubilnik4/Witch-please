package shared.models.tarot.authorize

import zio.json.*
import sttp.tapir.Schema

enum Role derives JsonCodec, Schema:
  case PreProject, Admin, User