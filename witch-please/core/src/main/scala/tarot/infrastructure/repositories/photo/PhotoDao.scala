package tarot.infrastructure.repositories.photo

import io.getquill.*
import io.getquill.jdbczio.*
import shared.models.tarot.photo.PhotoOwnerType
import tarot.domain.entities.PhotoEntity
import tarot.infrastructure.repositories.TarotTableNames
import tarot.infrastructure.repositories.spreads.SpreadQuillMappings
import zio.ZIO

import java.sql.SQLException
import java.util.UUID

final class PhotoDao(quill: Quill.Postgres[SnakeCase]) {
  import PhotoQuillMappings.given
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