package tarot.infrastructure.repositories.photo

import io.getquill.*
import io.getquill.jdbczio.*
import tarot.domain.entities.PhotoObjectEntity
import tarot.domain.models.photo.PhotoObject
import tarot.infrastructure.repositories.TarotTableNames
import zio.ZIO

import java.sql.SQLException
import java.util.UUID

final class PhotoObjectDao(quill: Quill.Postgres[SnakeCase]) {
  import PhotoQuillMappings.given
  import quill.*

  def getPhotoObject(photoObjectId: UUID): ZIO[Any, SQLException, Option[PhotoObjectEntity]] =
    run(
      quote {
        photoObjectTable
          .filter(_.id == lift(photoObjectId))
          .take(1)
      }
    ).map(_.headOption)

  def getPhotoObjectByHash(hash: String): ZIO[Any, SQLException, Option[PhotoObjectEntity]] =
    run(
      quote {
        photoObjectTable
          .filter(_.hash == lift(hash))
          .take(1)
      }
    ).map(_.headOption)

  def insertPhotoObject(photoObject: PhotoObjectEntity): ZIO[Any, SQLException, UUID] =
    run(
      quote {
        photoObjectTable
          .insertValue(lift(photoObject))
          .returning(_.id)
      }
    )

  def findOrCreatePhotoObjectId(photoObject: PhotoObject): ZIO[Any, SQLException, UUID] =
    for {
      existing <- getPhotoObjectByHash(photoObject.hash)
      photoObjectId <- existing match {
        case Some(found) => ZIO.succeed(found.id)
        case None => insertPhotoObject(PhotoObjectEntity.toEntity(UUID.randomUUID(), photoObject))
      }
    } yield photoObjectId

  def deletePhotoObject(photoObjectId: UUID): ZIO[Any, SQLException, Long] =
    run(
      quote {
        photoObjectTable
          .filter(_.id == lift(photoObjectId))
          .delete
      }
    )

  private inline def photoObjectTable =
    quote(querySchema[PhotoObjectEntity](TarotTableNames.photoObjects))
}
