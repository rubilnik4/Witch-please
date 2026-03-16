package bot.models

import zio.ZIO

final case class TestBotState(
  photoId: Option[String],
  clientSecret: Option[String]
)

object TestBotState {
  def photoId(state: TestBotState): ZIO[Any, Throwable, String] =
    ZIO.fromOption(state.photoId)
      .orElseFail(new RuntimeException("photoId not set"))
}
