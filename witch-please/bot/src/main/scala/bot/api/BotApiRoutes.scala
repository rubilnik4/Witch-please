package bot.api

import zio.*
import zio.http.*

case object BotApiRoutes {
  final val apiPath: String = "api"

  def postWebhookPath(baseUrl: String): URL =
    URL(Path.root / baseUrl / apiPath / "webhook")
}
