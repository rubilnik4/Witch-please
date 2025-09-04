package tarot.application.commands

import tarot.domain.models.TarotError
import tarot.layers.TarotEnv
import zio.ZIO

trait CommandHandler[-Cmd, +Res] {
  def handle(command: Cmd): ZIO[TarotEnv, TarotError, Res]
}
