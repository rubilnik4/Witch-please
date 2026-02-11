package tarot.application.commands.cards.commands

import shared.models.photo.PhotoSource
import tarot.domain.models.spreads.SpreadId

final case class CreateCardCommand(
  position: Int,
  spreadId: SpreadId,
  title: String,
  description: String,
  photo: PhotoSource
)
