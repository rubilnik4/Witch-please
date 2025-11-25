package bot.application.handlers.telegram

import bot.application.commands.telegram.{SchedulerCommands, TelegramCommands}
import bot.application.commands.{BotCommand, TarotCommand}

import java.util.UUID
import scala.util.Try

object TelegramCommandParser {
  def handle(command: String): BotCommand =
    command.trim match {
      case command if command.startsWith(SchedulerCommands.Prefix) =>
        TelegramSchedulerParser.handle(command)
      case command if command.startsWith(TelegramCommands.StubCommand) =>
        BotCommand.Noop
      case command if command.startsWith(TelegramCommands.Help) =>
        BotCommand.Help
      case command =>
        handleTarotCommand(command)
      }
    }

  private def handleTarotCommand(command: String): BotCommand =
    command.split("\\s+").toList match {
      case List(TelegramCommands.Start) =>
        TarotCommand.Start
      case List(TelegramCommands.AuthorStart) =>
        TarotCommand.AdminStart
      case List(TelegramCommands.ClientStart) =>
        TarotCommand.UserStart
      case List(TelegramCommands.AuthorSpreadCreate) =>
        TarotCommand.CreateSpread
      case TelegramCommands.AuthorSpreadSelect :: spreadIdStr :: cardCountStr :: Nil =>
        (Try(UUID.fromString(spreadIdStr)).toOption, cardCountStr.toIntOption) match {
          case (Some(spreadId), Some(cardCount)) =>
            TarotCommand.SelectSpread(spreadId, cardCount)
          case _ =>
            BotCommand.Unknown
        }
      case TelegramCommands.AuthorSpreadCardsSelect :: spreadIdStr :: Nil =>
        Try(UUID.fromString(spreadIdStr)).toOption match {
          case Some(spreadId) =>
            TarotCommand.SelectSpreadCards(spreadId)
          case _ =>
            BotCommand.Unknown
        }
      case TelegramCommands.AuthorSpreadPublish :: spreadIdStr :: Nil =>
        Try(UUID.fromString(spreadIdStr)).toOption match {
          case Some(spreadId) =>
            TarotCommand.PublishSpread(spreadId)
          case _ =>
            BotCommand.Unknown
        }
      case TelegramCommands.AuthorSpreadDelete :: spreadIdStr :: Nil =>
        Try(UUID.fromString(spreadIdStr)).toOption match {
          case Some(spreadId) =>
            TarotCommand.DeleteSpread(spreadId)
          case _ =>
            BotCommand.Unknown
        }  
      case TelegramCommands.AuthorCardCreate :: indexStr :: Nil =>
        indexStr.toIntOption match {
          case Some(index) =>
            TarotCommand.CreateCard(index - 1)
          case _ =>
            BotCommand.Unknown
        }
      case List(_, _*) | Nil =>
        BotCommand.Unknown
}
