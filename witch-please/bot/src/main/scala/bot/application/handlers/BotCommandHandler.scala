package bot.application.handlers

object BotCommandHandler {
  def handle(raw: String): BotCommand = {
    raw.trim.split("\\s+").toList match {
      case "/start" :: Nil => BotCommand.Start
      case "/help" :: Nil => BotCommand.Help
      case "/project_create" :: name => BotCommand.CreateProject(name.mkString(" "))
      case "/spread_confirm" :: idStr =>
        Try(UUID.fromString(idStr)).toOption match {
          case Some(id) => BotCommand.ConfirmSpread(id)
          case None => BotCommand.Unknown
        }
      case _ => BotCommand.Unknown
    }
  }
}
