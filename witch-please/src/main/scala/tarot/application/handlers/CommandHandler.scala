package tarot.application.handlers

import tarot.domain.models.TarotError
import tarot.layers.AppEnv
import zio.ZIO

trait CommandHandler[-Cmd, +Res] {
  def handle(command: Cmd): ZIO[AppEnv, TarotError, Res]
}
