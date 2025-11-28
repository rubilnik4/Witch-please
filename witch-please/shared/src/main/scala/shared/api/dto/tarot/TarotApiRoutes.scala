package shared.api.dto.tarot

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

  def userGetByClientIdPath(baseUrl: String, clientId: String): URL =
    make(baseUrl, apiPath, "user", "by-client", clientId)

  def authorCreatePath(baseUrl: String): URL =
    make(baseUrl, apiPath, "author")
    
  def authorsGetPath(baseUrl: String): URL =
    make(baseUrl, apiPath, "author")   

  def spreadCreatePath(baseUrl: String): URL =
    make(baseUrl, apiPath, "spread")

  def spreadGetPath(baseUrl: String, spreadId: UUID): URL =    
    make(baseUrl, apiPath, "spread", spreadId.toString)
    
  def spreadsGetPath(baseUrl: String): URL =
    make(baseUrl, apiPath, "spread")

  def spreadUpdatePath(baseUrl: String, spreadId: UUID): URL =
    make(baseUrl, apiPath, "spread", spreadId.toString)
    
  def spreadPublishPath(baseUrl: String, spreadId: UUID): URL =
    make(baseUrl, apiPath, "spread", spreadId.toString, "publish")

  def spreadDeletePath(baseUrl: String, spreadId: UUID): URL =
    make(baseUrl, apiPath, "spread", spreadId.toString)
    
  def cardCreatePath(baseUrl: String, spreadId: UUID, index: Int): URL =
    make(baseUrl, apiPath, "spread", spreadId.toString, "cards", index.toString)

  def cardsGetPath(baseUrl: String, spreadId: UUID): URL =
    make(baseUrl, apiPath, "card", "by-spread", spreadId.toString)

  def cardsCountGetPath(baseUrl: String, spreadId: UUID): URL =
    make(baseUrl, apiPath, "card", "by-spread", spreadId.toString, "count")
    
  def tokenAuthPath(baseUrl: String): URL =
    make(baseUrl, apiPath, "auth")
}
