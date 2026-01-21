package tarot.application.queries.users

import tarot.domain.models.TarotError
import tarot.domain.models.spreads.Spread
import tarot.domain.models.users.{Author, User}
import tarot.layers.TarotEnv
import zio.ZIO

trait UserQueryHandler {
  def getUserByClientId(clientId: String): ZIO[TarotEnv, TarotError, User]
  def getAuthors: ZIO[TarotEnv, TarotError, List[Author]]
}
 
