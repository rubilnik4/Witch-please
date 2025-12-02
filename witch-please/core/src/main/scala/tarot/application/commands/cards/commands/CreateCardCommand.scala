package tarot.application.commands.cards.commands

import tarot.domain.models.photo.PhotoSource
import tarot.domain.models.spreads.SpreadId

final case class CreateCardCommand(
  index: Int,
  spreadId: SpreadId,
  title: String,
  photo: PhotoSource
)
