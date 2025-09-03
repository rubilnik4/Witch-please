package tarot.application.queries

import tarot.domain.models.TarotError
import tarot.layers.AppEnv
import zio.ZIO

trait QueryHandler[-Qry, +Res] {
  def handle(query: Qry): ZIO[AppEnv, TarotError, Res]
}
