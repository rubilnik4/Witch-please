package shared.api.dto.tarot

import shared.api.dto.ApiRoutes
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
  val photos = "photos"

  def userGetByClientIdPath(baseUrl: String, clientId: String): URL =
    ApiRoutes.make(baseUrl, apiPath, users, "by-client", clientId)

  def authorCreatePath(baseUrl: String): URL =
    ApiRoutes.make(baseUrl, apiPath, authors)
    
  def authorsGetPath(baseUrl: String): URL =
    ApiRoutes.make(baseUrl, apiPath, authors)

  def channelCreatePath(baseUrl: String): URL =
    ApiRoutes.make(baseUrl, apiPath, channels)

  def channelUpdatePath(baseUrl: String, userChannelId: UUID): URL =
    ApiRoutes.make(baseUrl, apiPath, channels, userChannelId.toString)

  def channelDefaultGetPath(baseUrl: String): URL =
    ApiRoutes.make(baseUrl, apiPath, channels, "default")
    
  def spreadCreatePath(baseUrl: String): URL =
    ApiRoutes.make(baseUrl, apiPath, spreads)

  def spreadGetPath(baseUrl: String, spreadId: UUID): URL =
    ApiRoutes.make(baseUrl, apiPath, spreads, spreadId.toString)
    
  def spreadsGetPath(baseUrl: String): URL =
    ApiRoutes.make(baseUrl, apiPath, spreads)

  def spreadUpdatePath(baseUrl: String, spreadId: UUID): URL =
    ApiRoutes.make(baseUrl, apiPath, spreads, spreadId.toString)
    
  def spreadPublishPath(baseUrl: String, spreadId: UUID): URL =
    ApiRoutes.make(baseUrl, apiPath, spreads, spreadId.toString, "publish")

  def spreadDeletePath(baseUrl: String, spreadId: UUID): URL =
    ApiRoutes.make(baseUrl, apiPath, spreads, spreadId.toString)

  def spreadClonePath(baseUrl: String, spreadId: UUID): URL =
    ApiRoutes.make(baseUrl, apiPath, spreads, spreadId.toString, "clone")
    
  def cardCreatePath(baseUrl: String, spreadId: UUID): URL =
    ApiRoutes.make(baseUrl, apiPath, spreads, spreadId.toString, cards)

  def cardUpdatePath(baseUrl: String, cardId: UUID): URL =
    ApiRoutes.make(baseUrl, apiPath, cards, cardId.toString)

  def cardDeletePath(baseUrl: String, cardId: UUID): URL =
    ApiRoutes.make(baseUrl, apiPath, cards, cardId.toString)

  def cardGetPath(baseUrl: String, cardId: UUID): URL =
    ApiRoutes.make(baseUrl, apiPath, cards, cardId.toString)

  def cardsGetPath(baseUrl: String, spreadId: UUID): URL =
    ApiRoutes.make(baseUrl, apiPath, spreads, spreadId.toString, cards)

  def cardsCountGetPath(baseUrl: String, spreadId: UUID): URL =
    ApiRoutes.make(baseUrl, apiPath, spreads, spreadId.toString, cards, "count")

  def cardOfDayCreatePath(baseUrl: String, spreadId: UUID): URL =
    ApiRoutes.make(baseUrl, apiPath, spreads, spreadId.toString, cardsOfDay)

  def cardOfDayUpdatePath(baseUrl: String, cardOfDayId: UUID): URL =
    ApiRoutes.make(baseUrl, apiPath, cardsOfDay, cardOfDayId.toString)

  def cardOfDayDeletePath(baseUrl: String, cardOfDayId: UUID): URL =
    ApiRoutes.make(baseUrl, apiPath, cardsOfDay, cardOfDayId.toString)

  def cardOfDayGetPath(baseUrl: String, cardOfDayId: UUID): URL =
    ApiRoutes.make(baseUrl, apiPath, cardsOfDay, cardOfDayId.toString)
    
  def cardOfDayBySpreadGetPath(baseUrl: String, spreadId: UUID): URL =
    ApiRoutes.make(baseUrl, apiPath, spreads, spreadId.toString, cardsOfDay)

  def photoGetPath(baseUrl: String, photoId: UUID): URL =
    ApiRoutes.make(baseUrl, apiPath, photos, photoId.toString)
    
  def tokenAuthPath(baseUrl: String): URL =
    ApiRoutes.make(baseUrl, apiPath, TarotApiRoutes.auth)
}
