package tarot.infrastructure.repositories

import io.getquill.MappedEncoding
import tarot.domain.models.photo.{PhotoOwnerType, PhotoStorageType}

object QuillMappings {
  given MappedEncoding[PhotoStorageType, String] = MappedEncoding(_.toString)
  given MappedEncoding[String, PhotoStorageType] = MappedEncoding(PhotoStorageType.valueOf)

  given MappedEncoding[PhotoOwnerType, String] = MappedEncoding(_.toString)
  given MappedEncoding[String, PhotoOwnerType] = MappedEncoding(PhotoOwnerType.valueOf)
}
