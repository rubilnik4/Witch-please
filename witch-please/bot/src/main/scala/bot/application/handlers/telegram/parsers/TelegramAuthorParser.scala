package bot.application.handlers.telegram.parsers

import bot.application.commands.{BotCommand, AuthorCommand}
import bot.application.commands.telegram.AuthorCommands

import java.util.UUID
import scala.util.Try

object TelegramAuthorParser {
  def handle(command: String): BotCommand =
    command.split("\\s+").toList match {
      case List(AuthorCommands.Start) =>
        AuthorCommand.Start
      case List(AuthorCommands.SpreadCreate) =>
        AuthorCommand.CreateSpread
      case AuthorCommands.SpreadEdit :: spreadIdStr :: Nil =>
        Try(UUID.fromString(spreadIdStr)).toOption match {
          case Some(spreadId) =>
            AuthorCommand.EditSpread(spreadId)
          case _ =>
            BotCommand.Unknown
        }
      case AuthorCommands.SpreadSelect :: spreadIdStr :: cardCountStr :: Nil =>
        (Try(UUID.fromString(spreadIdStr)).toOption, cardCountStr.toIntOption) match {
          case (Some(spreadId), Some(cardCount)) =>
            AuthorCommand.SelectSpread(spreadId, cardCount)
          case _ =>
            BotCommand.Unknown
        }
      case AuthorCommands.SpreadCardsSelect :: spreadIdStr :: Nil =>
        Try(UUID.fromString(spreadIdStr)).toOption match {
          case Some(spreadId) =>
            AuthorCommand.SelectSpreadCards(spreadId)
          case _ =>
            BotCommand.Unknown
        }
      case AuthorCommands.SpreadPublish :: spreadIdStr :: Nil =>
        Try(UUID.fromString(spreadIdStr)).toOption match {
          case Some(spreadId) =>
            AuthorCommand.PublishSpread(spreadId)
          case _ =>
            BotCommand.Unknown
        }
      case AuthorCommands.SpreadDelete :: spreadIdStr :: Nil =>
        Try(UUID.fromString(spreadIdStr)).toOption match {
          case Some(spreadId) =>
            AuthorCommand.DeleteSpread(spreadId)
          case _ =>
            BotCommand.Unknown
        }
      case AuthorCommands.CardCreate :: positionStr :: Nil =>
        positionStr.toIntOption match {
          case Some(position) =>
            AuthorCommand.CreateCard(position - 1)
          case _ =>
            BotCommand.Unknown
        }
      case AuthorCommands.CardEdit :: cardIdStr :: Nil =>
        Try(UUID.fromString(cardIdStr)).toOption match {
          case Some(cardId) =>
            AuthorCommand.EditCard(cardId)
          case _ =>
            BotCommand.Unknown
        }
      case AuthorCommands.CardDelete :: cardIdStr :: Nil =>
        Try(UUID.fromString(cardIdStr)).toOption match {
          case Some(cardId) =>
            AuthorCommand.DeleteCard(cardId)
          case _ =>
            BotCommand.Unknown
        }
      case AuthorCommands.CardSelect :: cardIdStr :: Nil =>
        Try(UUID.fromString(cardIdStr)).toOption match {
          case Some(cardId) =>
            AuthorCommand.SelectCard(cardId)
          case _ =>
            BotCommand.Unknown
        }
      case _ =>
        BotCommand.Unknown
    }
}
