package tarot.infrastructure.repositories.spreads

import io.getquill.*
import io.getquill.jdbczio.*
import shared.models.tarot.photo.PhotoOwnerType
import tarot.domain.entities.PhotoEntity
import tarot.domain.models.photo.PhotoStorageType
import tarot.infrastructure.repositories.TarotTableNames
import zio.ZIO

import java.sql.SQLException
import java.util.UUID

final class PhotoDao(quill: Quill.Postgres[SnakeCase]) {
  import SpreadQuillMappings.given
  import quill.*

  def insertPhoto(photo: PhotoEntity): ZIO[Any, SQLException, UUID] =
    run(
      quote {
        photoTable
          .insertValue(lift(photo))
          .returning(_.id)
      })

  private inline def photoTable =
    quote(querySchema[PhotoEntity](TarotTableNames.photos))
}