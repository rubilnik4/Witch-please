package tarot.application.commands.spreads.commands

import tarot.domain.models.authorize.UserId
import tarot.domain.models.photo.{Photo, PhotoFile}
import tarot.domain.models.projects.ProjectId
import tarot.domain.models.spreads.SpreadId

import java.time.*

final case class UpdateSpreadCommand(
  spreadId: SpreadId,
  title: String,
  cardCount: Int,
  photo: PhotoFile
)