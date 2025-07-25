package tarot.infrastructure.repositories.spreads

import io.getquill.*
import io.getquill.jdbczio.Quill
import tarot.domain.entities.*
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.DatabaseError
import tarot.domain.models.cards.{Card, CardId}
import tarot.domain.models.photo.Photo
import tarot.domain.models.spreads.{Spread, SpreadId, SpreadStatusUpdate}
import zio.*

import java.sql.SQLException
import java.util.UUID

final class SpreadRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends SpreadRepository {
  private val spreadDao = SpreadDao(quill)
  private val cardDao = CardDao(quill)
  private val photoDao = PhotoDao(quill)

  def createSpread(spread: Spread): ZIO[Any, TarotError, SpreadId] =
    quill.transaction {
      for {
        photoId <- createPhoto(spread.coverPhoto)
        spreadEntity = SpreadEntity.toEntity(spread, photoId)
        spreadId <- spreadDao.insertSpread(spreadEntity)
      } yield SpreadId(spreadId)
    }
    .mapError(e => DatabaseError(s"Failed to create spread ${spread.id}", e.getCause))
    .tapBoth(
      e => ZIO.logErrorCause(s"Failed to create spread $spread to database", Cause.fail(e.ex)),
      _ => ZIO.logDebug(s"Successfully create spread $spread to database")
    )

  def updateSpreadStatus(spreadStatusUpdate: SpreadStatusUpdate): ZIO[Any, TarotError, Unit] =
    spreadDao
      .updateSpreadStatus(spreadStatusUpdate)
      .mapBoth(
        e => DatabaseError("Failed to update spread status", e),
        _ => ())
      .tapBoth(
        e => ZIO.logErrorCause(s"Failed to update spread status $spreadStatusUpdate to database", Cause.fail(e.ex)),
        _ => ZIO.logDebug(s"Successfully update spread status $spreadStatusUpdate to database")
      )

  def getSpread(spreadId: SpreadId): ZIO[Any, TarotError, Option[Spread]] =
    spreadDao
      .getSpread(spreadId.id)
      .mapError(e => DatabaseError("Failed to get spread", e))
      .tapBoth(
        e => ZIO.logErrorCause(s"Failed to get spread $spreadId from database", Cause.fail(e.ex)),
        _ => ZIO.logDebug(s"Successfully get spread $spreadId from database")
      ).flatMap {
        case Some(entity) => SpreadPhotoEntity.toDomain(entity).map(Some(_))
        case None => ZIO.none
      }

  def existsSpread(spreadId: SpreadId): ZIO[Any, TarotError, Boolean] =
    spreadDao
      .existsSpread(spreadId.id)
      .mapError(e => DatabaseError(s"Failed to check spread $spreadId", e))
      .tapBoth(
        e => ZIO.logErrorCause(s"Failed to check spread $spreadId from database", Cause.fail(e.ex)),
        _ => ZIO.logDebug(s"Successfully check spread $spreadId from database")
      )

  def validateSpread(spreadId: SpreadId): ZIO[Any, TarotError, Boolean] =
    spreadDao
      .validateSpread(spreadId.id)
      .mapError(e => DatabaseError(s"Failed to validate spread $spreadId", e))
      .tapBoth(
        e => ZIO.logErrorCause(s"Failed to validate spread $spreadId from database", Cause.fail(e.ex)),
        _ => ZIO.logDebug(s"Successfully validate spread $spreadId from database")
      )

  def createCard(card: Card): ZIO[Any, TarotError, CardId] =
    quill.transaction {
      for {
        photoId <- createPhoto(card.coverPhoto)

        cardEntity = CardEntity.toEntity(card, photoId)
        cardId <- cardDao.insertCard(cardEntity)
      } yield CardId(cardId)
    }
    .mapError(e => DatabaseError(s"Failed to create card ${card.id}", e.getCause))
    .tapBoth(
      e => ZIO.logErrorCause(s"Failed to create card $card to database", Cause.fail(e.ex)),
      _ => ZIO.logDebug(s"Successfully create card $card to database")
    )

  def countCards(spreadId: SpreadId): ZIO[Any, TarotError, Long] =
    cardDao
      .countCards(spreadId.id)
      .mapError(e => DatabaseError("Failed to count cards", e))
      .tapBoth(
        e => ZIO.logErrorCause(s"Failed to count cards for spread $spreadId from database", Cause.fail(e.ex)),
        _ => ZIO.logDebug(s"Successfully count cards for spread $spreadId from database")
      )

  private def createPhoto(photo: Photo): ZIO[Any, SQLException, UUID] =
    photoDao.insertPhoto(PhotoEntity.toEntity(photo))
}
