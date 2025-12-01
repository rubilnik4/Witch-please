package bot.api

import zio.*
import zio.http.*

import java.util.UUID

case object BotApiRoutes {
  final val apiPath: String = "api"

  def postWebhookPath(baseUrl: String): URL =
    URL(Path.root / baseUrl / apiPath / "webhook")
}
