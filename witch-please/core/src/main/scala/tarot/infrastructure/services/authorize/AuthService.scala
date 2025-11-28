package tarot.infrastructure.services.authorize

import shared.models.tarot.authorize.ClientType
import tarot.api.dto.tarot.authorize.TokenPayload
import tarot.application.commands.users.commands.CreateAuthorCommand
import tarot.domain.models.TarotError
import tarot.domain.models.authorize.{Token, UserId}
import tarot.domain.models.projects.ProjectId
import tarot.layers.TarotEnv
import zio.ZIO

trait AuthService {
  def issueToken(clientType: ClientType, userId: UserId, clientSecret: String): ZIO[TarotEnv, TarotError, Token]
  def validateToken(token: String): ZIO[TarotEnv, TarotError, TokenPayload]
}
