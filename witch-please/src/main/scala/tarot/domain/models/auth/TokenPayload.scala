package tarot.domain.models.auth

import tarot.domain.models.auth.{ClientType, Role}
import zio.json.*
import zio.schema.*

final case class TokenPayload(
  clientType: ClientType,
  userId: UserId,                            
  projectId: Option[String], 
  role: Role
)
