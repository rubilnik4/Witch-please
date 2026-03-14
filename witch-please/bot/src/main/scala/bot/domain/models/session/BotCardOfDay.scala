package bot.domain.models.session

import shared.models.tarot.spreads.SpreadStatus

import java.util.UUID

final case class BotCardOfDay(
  cardOfDayId: UUID, 
  snapShot: CardOfDaySnapshot
)
