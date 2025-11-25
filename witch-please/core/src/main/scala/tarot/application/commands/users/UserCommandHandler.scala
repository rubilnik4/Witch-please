package tarot.application.commands.users

import tarot.domain.models.TarotError
import tarot.domain.models.authorize.{ExternalUser, UserId}
import tarot.layers.TarotEnv
import zio.ZIO

trait UserCommandHandler {
  def createAuthor(externalUser: ExternalUser): ZIO[TarotEnv, TarotError, UserId]
}
