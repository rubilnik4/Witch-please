package shared.api.dto.tarot

import zio.*
import zio.http.*

import java.util.UUID

case object TarotApiRoutes {
  val apiPath = "api"
  val spreads = "spreads"
  val cards = "cards"
  val cardsOfDay = "cards-of-day"
  val authors = "authors"
  val auth = "auth"
  val users ="users"
  val channels = "channel"

  private def make(baseUrl: String, segments: String*): URL = {
    val base = baseUrl.stripSuffix("/")
    val path = segments.map(_.stripPrefix("/")).mkString("/")
    URL.decode(s"$base/$path").getOrElse {
      throw new IllegalArgumentException(s"Invalid URL: $base/$path")
    }
  }  

  def userGetByClientIdPath(baseUrl: String, clientId: String): URL =
    make(baseUrl, apiPath, users, "by-client", clientId)

  def authorCreatePath(baseUrl: String): URL =
    make(baseUrl, apiPath, authors)
    
  def authorsGetPath(baseUrl: String): URL =
    make(baseUrl, apiPath, authors)   

  def spreadCreatePath(baseUrl: String): URL =
    make(baseUrl, apiPath, spreads)

  def spreadGetPath(baseUrl: String, spreadId: UUID): URL =    
    make(baseUrl, apiPath, spreads, spreadId.toString)
    
  def spreadsGetPath(baseUrl: String): URL =
    make(baseUrl, apiPath, spreads)

  def spreadUpdatePath(baseUrl: String, spreadId: UUID): URL =
    make(baseUrl, apiPath, spreads, spreadId.toString)
    
  def spreadPublishPath(baseUrl: String, spreadId: UUID): URL =
    make(baseUrl, apiPath, spreads, spreadId.toString, "publish")

  def spreadDeletePath(baseUrl: String, spreadId: UUID): URL =
    make(baseUrl, apiPath, spreads, spreadId.toString)
    
  def cardCreatePath(baseUrl: String, spreadId: UUID): URL =
    make(baseUrl, apiPath, spreads, spreadId.toString, cards)

  def cardUpdatePath(baseUrl: String, cardId: UUID): URL =
    make(baseUrl, apiPath, cards, cardId.toString)

  def cardDeletePath(baseUrl: String, cardId: UUID): URL =
    make(baseUrl, apiPath, cards, cardId.toString)

  def cardGetPath(baseUrl: String, cardId: UUID): URL =
    make(baseUrl, apiPath, cards, cardId.toString)

  def cardsGetPath(baseUrl: String, spreadId: UUID): URL =
    make(baseUrl, apiPath, spreads, spreadId.toString, cards)

  def cardsCountGetPath(baseUrl: String, spreadId: UUID): URL =
    make(baseUrl, apiPath, spreads, spreadId.toString, cards, "count")

  def cardOfDayCreatePath(baseUrl: String, spreadId: UUID): URL =
    make(baseUrl, apiPath, spreads, spreadId.toString, cardsOfDay)

  def cardOfDayUpdatePath(baseUrl: String, cardOfDayId: UUID): URL =
    make(baseUrl, apiPath, cardsOfDay, cardOfDayId.toString)

  def cardOfDayDeletePath(baseUrl: String, cardOfDayId: UUID): URL =
    make(baseUrl, apiPath, cardsOfDay, cardOfDayId.toString)

  def cardOfDayGetPath(baseUrl: String, cardOfDayId: UUID): URL =
    make(baseUrl, apiPath, cardsOfDay, cardOfDayId.toString)
    
  def cardOfDayBySpreadGetPath(baseUrl: String, spreadId: UUID): URL =
    make(baseUrl, apiPath, spreads, spreadId.toString, cardsOfDay)

  def tokenAuthPath(baseUrl: String): URL =
    make(baseUrl, apiPath, TarotApiRoutes.auth)
}
