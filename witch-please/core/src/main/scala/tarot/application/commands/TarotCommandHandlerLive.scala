package tarot.application.commands

import tarot.application.commands.cardsOfDay.CardOfDayCommandHandler
import tarot.application.commands.cards.CardCommandHandler
import tarot.application.commands.channels.UserChannelCommandHandler
import tarot.application.commands.photos.PhotoCommandHandler
import tarot.application.commands.projects.*
import tarot.application.commands.spreads.*
import tarot.application.commands.users.*

final case class TarotCommandHandlerLive(
  userCommandHandler: UserCommandHandler,
  userChannelCommandHandler: UserChannelCommandHandler,                                
  projectCommandHandler: ProjectCommandHandler,
  spreadCommandHandler: SpreadCommandHandler,
  cardCommandHandler: CardCommandHandler,
  cardOfDayCommandHandler: CardOfDayCommandHandler,
  photoCommandHandler: PhotoCommandHandler
) extends TarotCommandHandler 
