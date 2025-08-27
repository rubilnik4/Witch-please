package shared.api.dto.tarot

import shared.models.tarot.contracts.*
import zio.*
import zio.http.*

import java.util.UUID

case object TarotApiRoutes {
  final val apiPath: String = "api"

  def userCreatePath(baseUrl: String): URL =
    URL(Path.root / baseUrl / apiPath / TarotChannelType.Telegram / "user")

  def projectCreatePath(baseUrl: String): URL =
    URL(Path.root / baseUrl / apiPath / "project")

  def spreadCreatePath(baseUrl: String): URL =
    URL(Path.root / baseUrl / apiPath / TarotChannelType.Telegram / "spread")

  def spreadPublishPath(baseUrl: String, spreadId: UUID): URL =
    URL(Path.root / baseUrl / apiPath / "spread" / spreadId.toString / "publish")

  def cardCreatePath(baseUrl: String, spreadId: UUID, index: Int): URL =
    URL(Path.root / baseUrl / apiPath / TarotChannelType.Telegram / "spread" / spreadId.toString / "cards" / index.toString)

  def tokenAuthPath(baseUrl: String): URL =
    URL(Path.root / baseUrl / apiPath / "auth")
}
