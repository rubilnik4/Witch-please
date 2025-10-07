package shared.api.dto.tarot

import shared.models.tarot.contracts.TarotChannelType
import zio.*
import zio.http.*

import java.util.UUID

case object TarotApiRoutes {
  val apiPath = "api"

  private def make(baseUrl: String, segments: String*): URL = {
    val base = baseUrl.stripSuffix("/")
    val path = segments.map(_.stripPrefix("/")).mkString("/")
    URL.decode(s"$base/$path").getOrElse {
      throw new IllegalArgumentException(s"Invalid URL: $base/$path")
    }
  }

  def userCreatePath(baseUrl: String): URL =
    make(baseUrl, apiPath, TarotChannelType.Telegram, "user")

  def userGetByClientIdPath(baseUrl: String, clientId: String): URL =
    make(baseUrl, apiPath, TarotChannelType.Telegram, "user", "by-client", clientId)

  def projectCreatePath(baseUrl: String): URL =
    make(baseUrl, apiPath, "project")

  def projectsGetPath(baseUrl: String, userId: UUID): URL =
    make(baseUrl, apiPath, "project", "by-user", userId.toString)

  def spreadCreatePath(baseUrl: String): URL =
    make(baseUrl, apiPath, TarotChannelType.Telegram, "spread")

  def spreadsGetPath(baseUrl: String, projectId: UUID): URL =
    make(baseUrl, apiPath, "spread", "by-project", projectId.toString)
    
  def spreadPublishPath(baseUrl: String, spreadId: UUID): URL =
    make(baseUrl, apiPath, "spread", spreadId.toString, "publish")

  def cardCreatePath(baseUrl: String, spreadId: UUID, index: Int): URL =
    make(baseUrl, apiPath, TarotChannelType.Telegram, "spread", spreadId.toString, "cards", index.toString)

  def tokenAuthPath(baseUrl: String): URL =
    make(baseUrl, apiPath, "auth")
}
