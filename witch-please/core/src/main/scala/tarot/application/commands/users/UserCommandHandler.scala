package tarot.application.commands.users

import tarot.application.commands.users.commands.CreateAuthorCommand
import tarot.domain.models.TarotError
import tarot.domain.models.authorize.UserId
import tarot.layers.TarotEnv
import zio.ZIO

trait UserCommandHandler {
  def createAuthor(command: CreateAuthorCommand): ZIO[TarotEnv, TarotError, UserId]
}
