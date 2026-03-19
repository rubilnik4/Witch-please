package tarot.infrastructure.repositories.cardsOfDay

import io.getquill.MappedEncoding
import shared.models.tarot.cardOfDay.CardOfDayStatus

object CardOfDayQuillMappings {
  given MappedEncoding[CardOfDayStatus, String] = MappedEncoding(_.toString)
  given MappedEncoding[String, CardOfDayStatus] = MappedEncoding(CardOfDayStatus.valueOf) 
}
