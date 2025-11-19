package bot.application.commands.telegram

import shared.api.dto.telegram.TelegramCommandRequest

import java.util.UUID

object TelegramCommands {
  final val Start = "/start"
  final val Help = "/help"
  final val UserStart = "/user_start"
  final val AdminStart = "/admin_start"
  final val ProjectCreate = "/project_create"
  final val ProjectSelect = "/project_select"
  final val SpreadCreate = "/spread_create"
  final val SpreadSelect = "/spread_select"
  final val SpreadCardsSelect = "/spread_cards_select"
  final val SpreadPublish = "/spread_publish"
  final val SpreadDelete = "/spread_delete"
  final val CardCreate = "/card_create"
  final val StubCommand = "/noop"

  def projectSelectCommand(projectId: UUID): String =
    s"${TelegramCommands.ProjectSelect} $projectId"
    
  def spreadSelectCommand(spreadId: UUID, cardCount: Int): String =
    s"${TelegramCommands.SpreadSelect} $spreadId $cardCount"

  def spreadCardsSelectCommand(spreadId: UUID): String =
    s"${TelegramCommands.SpreadCardsSelect} $spreadId"

  def spreadPublishCommand(spreadId: UUID): String =    
    s"${TelegramCommands.SpreadPublish} $spreadId"

  def spreadDeleteCommand(spreadId: UUID): String =
    s"${TelegramCommands.SpreadDelete} $spreadId"  
    
  def cardCreateCommand(index: Int): String =
    s"${TelegramCommands.CardCreate} $index"

  final val Commands: List[TelegramCommandRequest] = List(
    TelegramCommandRequest(stripSlash(Start), "Начать заново"),
    TelegramCommandRequest(stripSlash(Help), "Помощь")
  )

  private def stripSlash(command: String): String =
    command.stripPrefix("/")
}
