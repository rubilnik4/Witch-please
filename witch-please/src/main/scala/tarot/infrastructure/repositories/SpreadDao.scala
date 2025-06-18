package tarot.infrastructure.repositories

import io.getquill.*
import io.getquill.jdbczio.*
import tarot.domain.entities.SpreadEntity
import tarot.domain.models.spreads.SpreadStatus
import zio.ZIO

import java.sql.SQLException
import java.time.Instant
import java.util.UUID

final class SpreadDao(quill: Quill.Postgres[SnakeCase]) {
  import quill.*
  import SpreadStatusMapping.given

  private inline def spreadTable = quote {
    querySchema[SpreadEntity](TarotTableNames.Spreads)
  }

  def insertSpread(spread: SpreadEntity): ZIO[Any, SQLException, UUID] =
    run(
      quote {
        spreadTable
          .insertValue(lift(spread))
          .returning(_.id)
      })
}

object SpreadStatusMapping:
  import io.getquill.MappedEncoding

  given encodeSpreadStatus: MappedEncoding[SpreadStatus, String] =
    MappedEncoding(_.toString)

  given decodeSpreadStatus: MappedEncoding[String, SpreadStatus] =
    MappedEncoding(SpreadStatus.valueOf)
