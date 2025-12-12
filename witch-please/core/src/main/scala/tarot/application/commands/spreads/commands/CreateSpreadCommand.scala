package tarot.application.commands.spreads.commands

import tarot.domain.models.authorize.UserId
import tarot.domain.models.photo.PhotoSource

final case class CreateSpreadCommand(
  userId: UserId,
  title: String,
  cardsCount: Integer,
  description: String,
  photo: PhotoSource
)