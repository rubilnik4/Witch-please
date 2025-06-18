package tarot.domain.entities

import tarot.domain.entities.PhotoStorageType.{Local, S3}
import tarot.domain.models.TarotError
import tarot.domain.models.photo.PhotoSource
import tarot.domain.models.spreads.Spread
import zio.ZIO
import zio.json.ast.Json
import zio.json.*

import java.util.UUID

enum PhotoStorageType { case Local, S3 }

final case class PhotoSourceEntity(
    id: UUID,
    storageType: PhotoStorageType,
    data: Json)

object PhotoSourceMapper {
  def toDomain(photoSource: PhotoSourceEntity): ZIO[Any, TarotError, PhotoSource] =
    ZIO.fromEither(
        photoSource.storageType match {
          case Local => photoSource.data.as[PhotoSource.Local]
          case S3 => photoSource.data.as[PhotoSource.S3]
      })
      .mapError(error => TarotError.SerializationError(s"Can't decode PhotoSource: $error"))

  def toEntity(photoSource: PhotoSource): ZIO[Any, TarotError, PhotoSourceEntity] =
    for {
      json <- ZIO.fromEither(photoSource.toJsonAST)
        .mapError(error => TarotError.SerializationError(s"Can't encode PhotoSource: $error"))
      storage <- detectStorage(photoSource)
    } yield PhotoSourceEntity(UUID.randomUUID(), storage, json)
        
  private def detectStorage(photoSource: PhotoSource): ZIO[Any, TarotError, PhotoStorageType] =
    photoSource match {
      case PhotoSource.Local => 
        ZIO.succeed(Local)
      case PhotoSource.S3 => 
        ZIO.succeed(S3)
      case PhotoSource.Telegram =>
        ZIO.fail(TarotError.SerializationError(s"Can't encode telegram PhotoSource"))
    }
}
