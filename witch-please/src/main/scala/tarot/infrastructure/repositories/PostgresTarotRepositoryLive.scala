package tarot.infrastructure.repositories

import io.getquill.*
import io.getquill.jdbczio.Quill
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.DatabaseError
import tarot.domain.models.contracts.SpreadId
import tarot.domain.models.entities.{SpreadEntity, SpreadMapper}
import tarot.domain.models.spreads.Spread
import zio.*

import java.util.UUID

final class PostgresTarotRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends TarotRepository {

  private val spreadDao = SpreadDao(quill)

  override def createSpread(spread: Spread): ZIO[Any, TarotError, SpreadId] =
    val spreadEntity = SpreadMapper.toEntity(spread)
    (for {      
      spreadId <- createSpread(spreadEntity)
    } yield spreadId)
      .tapBoth(
        e => ZIO.logErrorCause(s"Failed to save spread $spread to database", Cause.fail(e)),
        _ => ZIO.logDebug(s"Successfully save spread $spread to database")
      )

  private def createSpread(spreadEntity: SpreadEntity): ZIO[Any, TarotError, SpreadId] =
    spreadDao
      .insertSpread(spreadEntity)
      .mapBoth(
        e => DatabaseError(s"Failed to save spread ${spreadEntity.id}", e),
        spreadId => SpreadId(spreadId)
      )
}
