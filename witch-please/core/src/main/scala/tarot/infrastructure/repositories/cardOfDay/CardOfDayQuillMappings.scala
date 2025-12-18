package tarot.infrastructure.repositories.cardOfDay

import io.getquill.MappedEncoding
import shared.models.tarot.cardOfDay.CardOfDayStatus
import shared.models.tarot.photo.PhotoOwnerType
import shared.models.tarot.spreads.SpreadStatus

object CardOfDayQuillMappings {
  given MappedEncoding[CardOfDayStatus, String] = MappedEncoding(_.toString)
  given MappedEncoding[String, CardOfDayStatus] = MappedEncoding(CardOfDayStatus.valueOf) 
}
