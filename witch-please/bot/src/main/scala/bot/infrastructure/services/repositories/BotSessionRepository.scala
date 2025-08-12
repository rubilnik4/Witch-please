package bot.infrastructure.services.repositories

trait BotSessionRepository {
  def get(chatId: Long): ZIO[Any, Throwable, Option[BotSession]]
  def upsert(chatId: Long, session: BotSession): ZIO[Any, Throwable, Unit]
  def modify(chatId: Long, session: BotSession): ZIO[Any, Throwable, BotSession]
}
