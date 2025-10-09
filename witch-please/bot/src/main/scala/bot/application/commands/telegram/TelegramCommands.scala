package bot.application.commands.telegram

import shared.api.dto.telegram.TelegramCommandRequest

object TelegramCommands {
  final val Start = "/start"
  final val Help = "/help"
  final val ProjectCreate = "/project_create"
  final val ProjectsGet = "/projects_get"
  final val SpreadCreate = "/spread_create"
  final val SpreadsGet = "/spreads_get"
  final val CardCreate = "/card_create"
  final val CardsGet = "/cards_get"
  final val SpreadPublish = "/spread_publish"

  final val Commands: List[TelegramCommandRequest] = List(
    TelegramCommandRequest(stripSlash(Start), "Начать заново"),
    TelegramCommandRequest(stripSlash(Help), "Помощь")
  )

  private def stripSlash(command: String): String =
    command.stripPrefix("/")
}
