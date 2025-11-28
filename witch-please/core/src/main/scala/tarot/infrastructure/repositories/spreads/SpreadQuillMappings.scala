package tarot.infrastructure.repositories.spreads

import io.getquill.MappedEncoding
import shared.models.tarot.photo.PhotoOwnerType
import shared.models.tarot.spreads.SpreadStatus

object SpreadQuillMappings {
  given MappedEncoding[SpreadStatus, String] = MappedEncoding(_.toString)
  given MappedEncoding[String, SpreadStatus] = MappedEncoding(SpreadStatus.valueOf) 
}
