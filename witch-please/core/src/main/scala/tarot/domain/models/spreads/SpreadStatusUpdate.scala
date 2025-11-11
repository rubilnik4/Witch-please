package tarot.domain.models.spreads

import shared.models.tarot.spreads.SpreadStatus
import java.time.Instant

sealed trait SpreadStatusUpdate(spreadId: SpreadId, spreadStatus: SpreadStatus) {
  override def toString: String = this match {
    case SpreadStatusUpdate.Ready(_, scheduledAt, expectedAt) =>
      s"ReadyUpdate(spreadId=$spreadId, scheduledAt=$scheduledAt, expectedAt=$expectedAt)"
    case SpreadStatusUpdate.Published(_, publishedAt) =>
      s"PublishedUpdate(spreadId=$spreadId, publishedAt=$publishedAt)"
  }
}

object SpreadStatusUpdate {
  case class Ready(spreadId: SpreadId, scheduledAt: Instant, expectedAt: Option[Instant])
    extends SpreadStatusUpdate(spreadId, SpreadStatus.Ready)

  case class Published(spreadId: SpreadId, publishedAt: Instant)
    extends SpreadStatusUpdate(spreadId, SpreadStatus.Published)
}