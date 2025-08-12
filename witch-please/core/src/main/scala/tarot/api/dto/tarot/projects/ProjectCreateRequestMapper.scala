package tarot.api.dto.tarot.projects

import shared.api.dto.tarot.projects.ProjectCreateRequest
import shared.models.tarot.authorize.ClientType
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.ValidationError
import tarot.domain.models.authorize.ExternalUser
import tarot.domain.models.projects.ExternalProject
import zio.json.*
import zio.schema.*
import zio.{IO, ZIO}

object ProjectCreateRequestMapper {
  def fromRequest(request: ProjectCreateRequest): IO[TarotError, ExternalProject] = {
    for {
      _ <- ZIO.fail(ValidationError("name must not be empty")).when(request.name.isEmpty)
    } yield toDomain(request)
  }

  private def toDomain(request: ProjectCreateRequest): ExternalProject =
    ExternalProject(
      name = request.name
    )
}