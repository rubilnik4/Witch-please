package tarot.api.endpoints

import tarot.api.endpoints.PathBuilder
import tarot.domain.models.contracts.TarotChannelType
import zio.http.{Root, int, uuid}

object SpreadPaths {  
  private final val spread = "spread"
  private final val cards = "cards"
  final val spreadTag = "spread"

  final val spreadCreatePath = 
    Root / PathBuilder.apiPath / TarotChannelType.Telegram / spread

  final val spreadPublishPath =
    Root / PathBuilder.apiPath / spread / uuid("spreadId") / "publish"

  final val cardCreatePath =
    Root / PathBuilder.apiPath / TarotChannelType.Telegram / spread / uuid("spreadId") / cards / int("index")
}
