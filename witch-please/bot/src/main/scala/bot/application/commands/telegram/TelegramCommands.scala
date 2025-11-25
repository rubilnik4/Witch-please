package bot.application.commands.telegram

import shared.api.dto.telegram.TelegramCommandRequest

import java.util.UUID

object TelegramCommands {
  final val Start = "/start"
  final val Help = "/help"
  final val StubCommand = "/noop"
  
  final val AuthorStart = "/author_start"
  final val AuthorSpreadCreate = "/author_spread_create"
  final val AuthorSpreadSelect = "/author_spread_select"
  final val AuthorSpreadCardsSelect = "/author_spread_cards_select"
  final val AuthorSpreadPublish = "/author_spread_publish"
  final val AuthorSpreadDelete = "/author_spread_delete"
  final val AuthorCardCreate = "/author_card_create"

  final val ClientStart = "/client_start"
  final val ClientAuthorSelect = "/client_author_select"

  final val DefaultCommands: List[TelegramCommandRequest] = List(
    TelegramCommandRequest(stripSlash(Start), "Начать заново"),
    TelegramCommandRequest(stripSlash(Help), "Помощь")
  )
  
  def authorSpreadSelect(spreadId: UUID, cardCount: Int): String =
    s"${TelegramCommands.AuthorSpreadSelect} $spreadId $cardCount"

  def authorSpreadCardsSelect(spreadId: UUID): String =
    s"${TelegramCommands.AuthorSpreadCardsSelect} $spreadId"

  def authorSpreadPublish(spreadId: UUID): String =    
    s"${TelegramCommands.AuthorSpreadPublish} $spreadId"

  def authorSpreadDelete(spreadId: UUID): String =
    s"${TelegramCommands.AuthorSpreadDelete} $spreadId"  
    
  def authorCardCreate(index: Int): String =
    s"${TelegramCommands.AuthorCardCreate} $index"

  def clientAuthorSelect(author: UUID): String =
    s"${TelegramCommands.ClientAuthorSelect} $author"
    
  private def stripSlash(command: String): String =
    command.stripPrefix("/")
}
