package tarot.infrastructure.repositories

import io.getquill.*
import io.getquill.jdbczio.Quill
import tarot.domain.models.TarotError.DatabaseError
import tarot.domain.entities.{PhotoSourceMapper, SpreadEntity, SpreadMapper}
import tarot.domain.models.TarotError
import tarot.domain.models.contracts.SpreadId
import tarot.domain.models.photo.PhotoSource
import tarot.domain.models.spreads.Spread
import zio.*

import java.util.UUID

final class PostgresTarotRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends TarotRepository {

  private val spreadDao = SpreadDao(quill)

  private val photoDao = PhotoDao(quill)

  override def createSpread(spread: Spread): ZIO[Any, TarotError, SpreadId] =
    (for {
      photoId <- createPhoto(spread.coverPhoto)

      spreadEntity = SpreadMapper.toEntity(spread, photoId)
      spreadId <- createSpread(spreadEntity)
    } yield spreadId)
      .tapBoth(
        e => ZIO.logErrorCause(s"Failed to create spread $spread to database", Cause.fail(e)),
        _ => ZIO.logDebug(s"Successfully create spread $spread to database")
      )

  private def createSpread(spreadEntity: SpreadEntity): ZIO[Any, TarotError, SpreadId] =
    spreadDao
      .insertSpread(spreadEntity)
      .mapBoth(
        e => DatabaseError(s"Failed to create spread ${spreadEntity.id}", e),
        spreadId => SpreadId(spreadId)
      )

  private def createPhoto(photo: PhotoSource): ZIO[Any, TarotError, UUID] =
    for {
      photoEntity <- PhotoSourceMapper.toEntity(photo)
      photoId <- photoDao.insertPhoto(photoEntity)
        .mapError(e => DatabaseError("Failed to create photo", e))
    } yield photoId  
}
