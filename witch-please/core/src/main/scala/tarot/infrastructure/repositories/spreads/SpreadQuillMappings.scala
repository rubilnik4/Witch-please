package tarot.infrastructure.repositories.spreads

import io.getquill.MappedEncoding
import shared.models.tarot.photo.PhotoOwnerType
import shared.models.tarot.spreads.SpreadStatus
import tarot.domain.models.photo.PhotoStorageType

object SpreadQuillMappings {
  given MappedEncoding[SpreadStatus, String] = MappedEncoding(_.toString)
  given MappedEncoding[String, SpreadStatus] = MappedEncoding(SpreadStatus.valueOf)
  
  given MappedEncoding[PhotoStorageType, String] = MappedEncoding(_.toString)
  given MappedEncoding[String, PhotoStorageType] = MappedEncoding(PhotoStorageType.valueOf)

  given MappedEncoding[PhotoOwnerType, String] = MappedEncoding(_.toString)
  given MappedEncoding[String, PhotoOwnerType] = MappedEncoding(PhotoOwnerType.valueOf)
}
