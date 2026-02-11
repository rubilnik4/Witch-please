package tarot.application.commands.spreads.commands

import shared.models.photo.PhotoSource
import tarot.domain.models.photo.Photo
import tarot.domain.models.projects.ProjectId
import tarot.domain.models.spreads.SpreadId
import tarot.domain.models.users.UserId

import java.time.*

final case class UpdateSpreadCommand(
  spreadId: SpreadId,
  title: String,
  cardCount: Int,
  description: String,
  photo: PhotoSource
)