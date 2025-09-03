package shared.api.dto.tarot.users

import zio.json.*
import zio.schema.*

final case class UserCreateRequest(
  clientId: String,
  clientSecret: String,
  name: String
) derives JsonCodec, Schema