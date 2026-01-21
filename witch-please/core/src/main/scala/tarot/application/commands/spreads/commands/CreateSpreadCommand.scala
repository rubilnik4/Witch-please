package tarot.application.commands.spreads.commands

import tarot.domain.models.photo.PhotoSource
import tarot.domain.models.users.UserId

final case class CreateSpreadCommand(
  userId: UserId,
  title: String,
  cardsCount: Integer,
  description: String,
  photo: PhotoSource
)