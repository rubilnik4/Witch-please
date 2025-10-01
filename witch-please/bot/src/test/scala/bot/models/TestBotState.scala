package bot.models

import java.util.UUID

final case class TestBotState(
  photoId: Option[String],
  clientSecret: Option[String]
)
