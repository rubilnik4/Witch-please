package tarot.infrastructure.services.auth

import tarot.api.dto.tarot.authorize.TokenPayload
import tarot.domain.models.TarotError
import tarot.domain.models.auth.{ClientType, ExternalUser, UserId}
import tarot.domain.models.projects.ProjectId
import tarot.layers.AppEnv
import zio.ZIO

trait AuthService {
  def issueToken(clientType: ClientType, userId: UserId, clientSecret: String, projectId: Option[ProjectId])
      : ZIO[AppEnv, TarotError, String]
  def validateToken(token: String): ZIO[AppEnv, TarotError, TokenPayload]
}
