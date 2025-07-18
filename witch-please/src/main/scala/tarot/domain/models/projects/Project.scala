package tarot.domain.models.projects

import tarot.infrastructure.services.common.DateTimeService
import zio.UIO

import java.time.Instant
import java.util.UUID

case class Project(
  id: ProjectId,
  name: String,
  createdAt: Instant,
)
{
  override def toString: String = id.toString
}

object Project {
  def toDomain(externalProject: ExternalProject): UIO[Project] =
    val id = UUID.randomUUID()
    for {
      createdAt <- DateTimeService.getDateTimeNow
      project = Project(
        id = ProjectId(id),
        name = externalProject.name,
        createdAt = createdAt)
    } yield project
}