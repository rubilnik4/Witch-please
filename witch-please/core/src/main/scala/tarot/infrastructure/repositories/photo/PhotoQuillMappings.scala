package tarot.infrastructure.repositories.photo

import io.getquill.MappedEncoding
import shared.models.files.*
import shared.models.tarot.photo.PhotoOwnerType

object PhotoQuillMappings {
  given MappedEncoding[PhotoOwnerType, String] = MappedEncoding(_.toString)
  given MappedEncoding[String, PhotoOwnerType] = MappedEncoding(PhotoOwnerType.valueOf)

  given MappedEncoding[FileSourceType, String] = MappedEncoding(_.toString)
  given MappedEncoding[String, FileSourceType] = MappedEncoding(FileSourceType.valueOf)

  given MappedEncoding[FileStoredType, String] = MappedEncoding(_.toString)
  given MappedEncoding[String, FileStoredType] = MappedEncoding(FileStoredType.valueOf)
}
