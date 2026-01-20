package tarot.infrastructure.repositories.cardsOfDay

import io.getquill.*
import io.getquill.extras.InstantOps
import io.getquill.jdbczio.*
import shared.models.tarot.cardOfDay.CardOfDayStatus
import tarot.domain.entities.{CardEntity, CardOfDayEntity, CardOfDayPhotoEntity, PhotoEntity, SpreadPhotoEntity}
import tarot.domain.models.cards.CardUpdate
import tarot.domain.models.cardsOfDay.CardOfDayUpdate
import tarot.infrastructure.repositories.TarotTableNames
import tarot.infrastructure.repositories.photo.PhotoQuillMappings
import zio.ZIO

import java.sql.SQLException
import java.time.Instant
import java.util.UUID

final class CardOfDayDao(quill: Quill.Postgres[SnakeCase]) {
  import PhotoQuillMappings.given
  import CardOfDayQuillMappings.given
  import quill.*

  def getCardOfDay(cardOfDayId: UUID): ZIO[Any, SQLException, Option[CardOfDayPhotoEntity]] =
    run(
      quote {
        cardOfDayTable
          .join(photoTable)
          .on((cardOfDay, photo) => cardOfDay.photoId == photo.id)
          .filter { case (cardOfDay, _) => cardOfDay.id == lift(cardOfDayId) }
          .take(1)
          .map { case (cardOfDay, photo) => CardOfDayPhotoEntity(cardOfDay, photo) }
      })
      .map(_.headOption)
      
  def getCardOfDayBySpread(spreadId: UUID): ZIO[Any, SQLException, Option[CardOfDayPhotoEntity]] =
    run(
      quote {
        cardOfDayTable
          .join(photoTable)
          .on((cardOfDay, photo) => cardOfDay.photoId == photo.id)
          .filter { case (cardOfDay, _) => cardOfDay.spreadId == lift(spreadId) }
          .take(1)
          .map { case (cardOfDay, photo) => CardOfDayPhotoEntity(cardOfDay, photo) }
      })
      .map(_.headOption)

  def getCardOfDayByCard(cardId: UUID): ZIO[Any, SQLException, Option[CardOfDayPhotoEntity]] =
    run(
      quote {
        cardOfDayTable
          .join(photoTable)
          .on((cardOfDay, photo) => cardOfDay.photoId == photo.id)
          .filter { case (cardOfDay, _) => cardOfDay.cardId == lift(cardId) }
          .take(1)
          .map { case (cardOfDay, photo) => CardOfDayPhotoEntity(cardOfDay, photo) }
      })
      .map(_.headOption)
      
  def getScheduledCardsOfDay(deadline: Instant, limit: Int): ZIO[Any, SQLException, List[CardOfDayPhotoEntity]] =
    run(
      quote {
        cardOfDayTable
          .join(photoTable)
          .on((spread, photo) => spread.photoId == photo.id)
          .filter { case (cardOfDay, _) =>
            cardOfDay.status == lift(CardOfDayStatus.Scheduled) &&
            cardOfDay.scheduledAt.exists(_ <= lift(deadline))
          }
          .sortBy { case (cardOfDay, _) => cardOfDay.scheduledAt }(Ord.asc)
          .take(lift(limit))
          .map { case (cardOfDay, photo) => CardOfDayPhotoEntity(cardOfDay, photo) }
      })
      
  def existCardOfDay(spreadId: UUID): ZIO[Any, SQLException, Boolean] =
    run(
      quote {
        cardOfDayTable
          .filter { cardOfDay => cardOfDay.spreadId == lift(spreadId) }
          .take(1)
          .nonEmpty
      })
  
  def insertCardOfDay(cardOfDay: CardOfDayEntity): ZIO[Any, SQLException, UUID] =
    run(
      quote {
        cardOfDayTable
          .insertValue(lift(cardOfDay))
          .returning(_.id)
      })

  def updateToSchedule(cardOfDayId: UUID, scheduleAt: Instant): ZIO[Any, SQLException, Long] =
    run(quote {
      cardOfDayTable
        .filter(cardOfDay => cardOfDay.id == lift(cardOfDayId) && isScheduleStatus(cardOfDay))
        .update(
          _.status -> lift(CardOfDayStatus.Scheduled),
          _.scheduledAt -> lift(Option(scheduleAt))
        )
    })

  def updateCardOfDay(cardOfDayId: UUID, cardOfDay: CardOfDayUpdate, photoId: UUID): ZIO[Any, SQLException, Long] =
    run(
      quote {
        cardOfDayTable
          .filter(_.id == lift(cardOfDayId))
          .update(
            _.cardId -> lift(cardOfDay.cardId.id),
            _.description -> lift(cardOfDay.description),
            _.photoId -> lift(photoId)
          )
      }
    )
    
  def updateToPublish(cardOfDayId: UUID, publishedAt: Instant): ZIO[Any, SQLException, Long] =
    run(quote {
      cardOfDayTable
        .filter(cardOfDay => cardOfDay.id == lift(cardOfDayId) && isPublishStatus(cardOfDay))
        .update(
          _.status -> lift(CardOfDayStatus.Published),
          _.publishedAt -> lift(Option(publishedAt))
        )
    })

  def deleteCardOfDay(cardOfDayId: UUID): ZIO[Any, SQLException, Long] =
    run(
      quote {
        cardOfDayTable
          .filter(_.id == lift(cardOfDayId))
          .delete
      })
      
  private inline def isScheduleStatus(cardOfDay: CardOfDayEntity) =
    quote(cardOfDay.status == lift(CardOfDayStatus.Draft) || cardOfDay.status == lift(CardOfDayStatus.Scheduled))

  private inline def isPublishStatus(cardOfDay: CardOfDayEntity) =
    quote(cardOfDay.status == lift(CardOfDayStatus.Scheduled) && cardOfDay.publishedAt.isEmpty)
    
  private inline def cardOfDayTable =
    quote(querySchema[CardOfDayEntity](TarotTableNames.cardsOfDay))

  private inline def photoTable =
    quote(querySchema[PhotoEntity](TarotTableNames.photos))
}
