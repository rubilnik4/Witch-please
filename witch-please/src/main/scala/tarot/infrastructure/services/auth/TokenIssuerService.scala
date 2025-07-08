package tarot.infrastructure.services.auth

import tarot.api.dto.tarot.auth.TokenPayload
import tarot.domain.models.TarotError
import tarot.domain.models.auth.ClientType
import tarot.layers.AppEnv
import zio.ZIO

trait TokenIssuerService {
  def issueToken(clientType: ClientType, userId: String, project: String, clientSecret: String)
      : ZIO[AppEnv, TarotError, String]
  def validateToken(token: String): ZIO[AppEnv, TarotError, TokenPayload]
}
