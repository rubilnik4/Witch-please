package tarot.domain.models.spreads

import shared.models.tarot.spreads.SpreadStatus
import java.time.Instant

sealed trait SpreadStatusUpdate(spreadId: SpreadId, spreadStatus: SpreadStatus) {
  override def toString: String = this match {
    case SpreadStatusUpdate.Ready(_, _, scheduledAt) =>
      s"ReadyUpdate(spreadId=$spreadId, scheduledAt=$scheduledAt)"
    case SpreadStatusUpdate.Published(_, _, publishedAt) =>
      s"PublishedUpdate(spreadId=$spreadId, publishedAt=$publishedAt)"
  }
}

object SpreadStatusUpdate {
  case class Ready(spreadId: SpreadId,spreadStatus: SpreadStatus, scheduledAt: Instant)
    extends SpreadStatusUpdate(spreadId, spreadStatus)

  case class Published(spreadId: SpreadId, spreadStatus: SpreadStatus, publishedAt: Instant)
    extends SpreadStatusUpdate(spreadId, spreadStatus)
}