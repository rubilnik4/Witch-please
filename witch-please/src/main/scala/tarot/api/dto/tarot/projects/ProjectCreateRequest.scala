package tarot.api.dto.tarot.projects

import tarot.api.dto.tarot.auth.UserCreateRequest
import tarot.domain.models.TarotError
import tarot.domain.models.TarotError.ValidationError
import tarot.domain.models.auth.{ClientType, ExternalUser}
import tarot.domain.models.projects.ExternalProject
import zio.json.*
import zio.schema.*
import zio.{IO, ZIO}

final case class ProjectCreateRequest(
  name: String
) derives JsonCodec, Schema

object ProjectCreateRequest {
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