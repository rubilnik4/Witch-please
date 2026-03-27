package tarot.domain.models.photo

import shared.models.files.*
import shared.models.photo.PhotoFile
import shared.models.photo.PhotoSource
import java.util.UUID

final case class Photo(
  id: PhotoId,
  photoObject: PhotoObject,
  sourceType: FileSourceType,
  sourceId: String
)

object Photo {
  def toPhoto(id: UUID, photoObject: PhotoObject, sourceType: FileSourceType, sourceId: String): Photo =
    Photo(PhotoId(id), photoObject, sourceType, sourceId)

  def create(photoFile: PhotoFile, sourceType: FileSourceType, sourceId: String): Photo = {
    val photoObject = PhotoObject.toPhotoObject(photoFile.hash, photoFile.fileStored)
    Photo(
      id = PhotoId(UUID.randomUUID()),
      photoObject = photoObject,
      sourceType = sourceType,
      sourceId = sourceId
    )
  }

  def toPhotoSource(photo: Photo): PhotoSource =
    toPhotoSource(photo, None)
    
  def toPhotoSource(photo: Photo, parentId: Option[String]): PhotoSource =
    PhotoSource(photo.sourceId, photo.sourceType, parentId)
}
