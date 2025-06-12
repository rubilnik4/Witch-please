package tarot

object MainApp extends ZIOAppDefault {
  override def run: ZIO[Any, Throwable, Unit] = {
    MainAppLayer.run
  }
}
