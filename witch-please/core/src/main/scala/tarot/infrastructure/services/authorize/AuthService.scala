package tarot.infrastructure.services.authorize

import tarot.api.dto.tarot.authorize.TokenPayload
import tarot.domain.models.TarotError
import tarot.domain.models.authorize.{ClientType, ExternalUser, Token, UserId}
import tarot.domain.models.projects.ProjectId
import tarot.layers.AppEnv
import zio.ZIO

trait AuthService {
  def issueToken(clientType: ClientType, userId: UserId, clientSecret: String, projectId: Option[ProjectId])
      : ZIO[AppEnv, TarotError, Token]
  def validateToken(token: String): ZIO[AppEnv, TarotError, TokenPayload]
}
