package tarot.api.endpoints

import tarot.api.dto.tarot.*
import tarot.api.dto.tarot.projects.ProjectCreateRequest
import tarot.api.dto.tarot.spreads.*
import tarot.api.dto.tarot.users.UserCreateRequest
import tarot.api.middlewares.AuthMiddleware
import tarot.application.commands.*
import tarot.application.commands.projects.ProjectCreateCommand
import tarot.application.commands.spreads.{CardCreateCommand, SpreadCreateCommand, SpreadPublishCommand}
import tarot.application.commands.users.UserCreateCommand
import tarot.domain.models.auth.{ClientType, Role}
import tarot.domain.models.contracts.TarotChannelType
import tarot.domain.models.spreads.SpreadId
import tarot.layers.AppEnv
import zio.ZIO
import zio.http.*
import zio.http.Method.*
import zio.http.codec.HttpCodec
import zio.http.endpoint.Endpoint

object ProjectEndpoint {
  private final val project = "project"
  private final val projectTag = "projects"

  private final val projectCreatePath =
    Root / PathBuilder.apiPath / project

  private val postProjectEndpoint =
    Endpoint(POST / projectCreatePath)
      .in[ProjectCreateRequest](MediaType.application.json)
      .out[String]
      .outErrors(
        HttpCodec.error[TarotErrorResponse](Status.BadRequest),
        HttpCodec.error[TarotErrorResponse](Status.NotFound),
        HttpCodec.error[TarotErrorResponse](Status.InternalServerError)
      )
      .tag(projectTag)

  private val postProjectRoute = postProjectEndpoint.implement { request =>
    (for {
      _ <- ZIO.logInfo(s"Received request to create project: ${request.name}")
      externalProject <- ProjectCreateRequest.fromRequest(request)

      projectCreateCommandHandler <- ZIO.serviceWith[AppEnv](_.tarotCommandHandler.projectCreateCommandHandler)
      projectCreateCommand = ProjectCreateCommand(externalProject)
      projectId <- projectCreateCommandHandler.handle(projectCreateCommand)
    } yield projectId)
      .mapBoth(
        error => TarotErrorResponse.toResponse(error),
        projectId => projectId.id.toString)
  }

  val allEndpoints: List[Endpoint[?, ?, ?, ?, ?]] =
    List(postProjectEndpoint)

  val allRoutes: Routes[AppEnv, Response] =
    Routes(postProjectRoute)
}
