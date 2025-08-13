package bot.domain.models.telegram

final case class TelegramContext(
  chatId: Long,
  clientId: Long,
  firstName: Option[String],
  username: Option[String]
)
