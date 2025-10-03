package tarot.api.dto.tarot.projects

import shared.api.dto.tarot.projects.ProjectResponse
import shared.api.dto.tarot.users.*
import shared.models.tarot.authorize.ClientType
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.ValidationError
import tarot.domain.models.authorize.{ExternalUser, User}
import tarot.domain.models.projects.Project
import zio.json.*
import zio.schema.*
import zio.{IO, ZIO}

object ProjectResponseMapper {
  def toResponse(project: Project): ProjectResponse =
    ProjectResponse(
      id = project.id.id,
      name = project.name,
      createdAt = project.createdAt,
    )
}