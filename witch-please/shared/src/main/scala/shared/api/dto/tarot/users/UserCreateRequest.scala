package shared.api.dto.tarot.users

import shared.models.tarot.authorize.ClientType
import zio.json.*
import zio.schema.*
import zio.{IO, ZIO}

final case class UserCreateRequest(
  clientId: String,
  clientSecret: String,
  name: String
) derives JsonCodec, Schema