package bot.application.commands.telegram

import shared.api.dto.telegram.TelegramCommandRequest

object TelegramCommands {
  val Start = "/start"
  val Help = "/help"
  val ProjectCreate = "/project_create"
  val ProjectsGet = "/projects_get"
  val SpreadCreate = "/spread_create"
  val CardCreate = "/card_create"
  val SpreadPublish = "/spread_publish"

  val Commands: List[TelegramCommandRequest] = List(
    TelegramCommandRequest(stripSlash(Start), "Начать заново"),
    TelegramCommandRequest(stripSlash(Help), "Помощь")
  )

  private def stripSlash(command: String): String =
    command.stripPrefix("/")
}
