package bot

import zio.{ZIO, ZIOAppDefault}

object MainApp extends ZIOAppDefault {
  override def run: ZIO[Any, Throwable, Unit] = {
    MainBotLayer.run
  }
}
