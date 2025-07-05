package tarot.api.endpoints

import tarot.domain.models.contracts.TarotChannelType
import zio.http.{Root, int, uuid}

object SpreadRoutes {
  private final val spread = "spread"
  private final val cards = "cards"
  final val spreadTag = "spread"

  final val spreadPath = Root / PathBuilder.apiPath / TarotChannelType.Telegram / spread

  final val publishSpreadPath =
    Root / PathBuilder.apiPath / TarotChannelType.Telegram / spread / uuid("spreadId") / "publish"

  final val cardPath =
    Root / PathBuilder.apiPath / TarotChannelType.Telegram / spread / uuid("spreadId") / cards / int("index")
}
