package tarot.application.commands.users

import tarot.domain.models.TarotError
import tarot.domain.models.authorize.{ExternalUser, UserId}
import tarot.layers.TarotEnv
import zio.ZIO

trait UserCreateCommandHandler {
  def createUser(externalUser: ExternalUser): ZIO[TarotEnv, TarotError, UserId]
}
