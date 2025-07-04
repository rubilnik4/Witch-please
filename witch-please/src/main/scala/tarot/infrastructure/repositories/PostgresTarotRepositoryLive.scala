package tarot.infrastructure.repositories

import io.getquill.*
import io.getquill.jdbczio.Quill
import tarot.domain.models.TarotError.DatabaseError
import tarot.domain.entities.{CardEntity, CardMapper, PhotoSourceMapper, SpreadEntity, SpreadMapper}
import tarot.domain.models.TarotError
import tarot.domain.models.cards.Card
import tarot.domain.models.contracts.{CardId, SpreadId}
import tarot.domain.models.photo.Photo
import tarot.domain.models.spreads.Spread
import zio.*

import java.sql.SQLException
import java.util.UUID

final class PostgresTarotRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends TarotRepository {
  import quill.*

  private val spreadDao = SpreadDao(quill)
  private val cardDao = CardDao(quill)
  private val photoDao = PhotoDao(quill)

  def createSpread(spread: Spread): ZIO[Any, TarotError, SpreadId] =
    quill.transaction {
      for {
        photoId <- createPhoto(spread.coverPhoto)

        spreadEntity = SpreadMapper.toEntity(spread, photoId)
        spreadId <- createSpread(spreadEntity)
      } yield spreadId
    }
    .mapError(e => DatabaseError(s"Failed to create spread ${spread.id}", e.getCause))
    .tapBoth(
      e => ZIO.logErrorCause(s"Failed to create spread $spread to database", Cause.fail(e)),
      _ => ZIO.logDebug(s"Successfully create spread $spread to database")
    )

  def getSpread(spreadId: SpreadId): ZIO[Any, TarotError, Option[Spread]] =
    spreadDao
      .getSpread(spreadId.id)
      .mapError(e => DatabaseError("Failed to get spread", e))
      .flatMap {
        case Some(spreadEntity) =>
          SpreadMapper.toDomain(spreadEntity).map(Some(_))
        case None =>
          ZIO.none
      }
      .tapBoth(
        e => ZIO.logErrorCause(s"Failed to get spread $spreadId from database", Cause.fail(e)),
        _ => ZIO.logDebug(s"Successfully get spread $spreadId from database")
      )

  def existsSpread(spreadId: SpreadId): ZIO[Any, TarotError, Boolean] =
    spreadDao
      .existsSpread(spreadId.id)
      .mapError(e => DatabaseError("Failed to check spread", e))
      .tapBoth(
        e => ZIO.logErrorCause(s"Failed to check spread $spreadId from database", Cause.fail(e)),
        _ => ZIO.logDebug(s"Successfully check spread $spreadId from database")
      )

  def createCard(card: Card): ZIO[Any, TarotError, CardId] =
    quill.transaction {
      for {
        photoId <- createPhoto(card.coverPhoto)

        cardEntity = CardMapper.toEntity(card, photoId)
        cardId <- createCard(cardEntity)
      } yield cardId
    }
    .mapError(e => DatabaseError(s"Failed to create card ${card.id}", e.getCause))
    .tapBoth(
      e => ZIO.logErrorCause(s"Failed to create card $card to database", Cause.fail(e)),
      _ => ZIO.logDebug(s"Successfully create card $card to database")
    )

  private def createSpread(spreadEntity: SpreadEntity): ZIO[Any, SQLException, SpreadId] =
    spreadDao
      .insertSpread(spreadEntity)
      .map(spreadId => SpreadId(spreadId))

  private def createCard(cardEntity: CardEntity): ZIO[Any, SQLException, CardId] =
    cardDao
      .insertCard(cardEntity)
      .map(cardId => CardId(cardId))

  private def createPhoto(photo: Photo): ZIO[Any, SQLException, UUID] = {
    val photoEntity = PhotoSourceMapper.toEntity(photo)
    for {
      photoId <- photoDao.insertPhoto(photoEntity)
    } yield photoId
  }
}
