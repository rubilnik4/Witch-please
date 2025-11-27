package bot.application.commands.telegram

import shared.api.dto.telegram.TelegramCommandRequest

import java.util.UUID

object TelegramCommands {
  final val Start = "/start"
  final val Help = "/help"
  final val StubCommand = "/noop"
  
  final val DefaultCommands: List[TelegramCommandRequest] = List(
    TelegramCommandRequest(Start, "Начать заново"),
    TelegramCommandRequest(Help, "Помощь")
  )
}
