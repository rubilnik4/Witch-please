package tarot.application.queries

import tarot.domain.models.TarotError
import tarot.layers.TarotEnv
import zio.ZIO

trait QueryHandler[-Qry, +Res] {
  def handle(query: Qry): ZIO[TarotEnv, TarotError, Res]
}
