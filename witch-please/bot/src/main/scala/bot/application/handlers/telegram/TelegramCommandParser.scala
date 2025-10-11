package bot.application.handlers.telegram

import bot.application.commands.BotCommand
import bot.application.commands.telegram.TelegramCommands

import java.time.Instant
import java.util.UUID
import scala.util.Try

object TelegramCommandParser {
  def handle(command: String): BotCommand =
    command.trim.split("\\s+").toList match {
      case List(TelegramCommands.Start) =>
        BotCommand.Start
      case List(TelegramCommands.Help) =>
        BotCommand.Help
      case List(TelegramCommands.ProjectCreate) =>
        BotCommand.CreateProject
      case TelegramCommands.ProjectSelect :: projectIdStr :: Nil =>
        Try(UUID.fromString(projectIdStr)).toOption match {
          case Some(projectId) =>
            BotCommand.SelectProject(projectId)
          case _ =>
            BotCommand.Unknown
        }
      case List(TelegramCommands.SpreadCreate) =>
        BotCommand.CreateSpread
      case TelegramCommands.SpreadSelect :: spreadIdStr :: cardCountStr :: Nil =>
        (Try(UUID.fromString(spreadIdStr)).toOption, cardCountStr.toIntOption) match {
          case (Some(spreadId), Some(cardCount)) =>
            BotCommand.SelectSpread(spreadId, cardCount)
          case _ =>
            BotCommand.Unknown
        }
      case TelegramCommands.CardCreate :: indexStr :: Nil =>
        indexStr.toIntOption match {
          case Some(index) =>
            BotCommand.CreateCard(index - 1)
          case _ =>
            BotCommand.Unknown
        }
      case TelegramCommands.CardSelect :: cardIdStr :: indexStr :: Nil =>
        (Try(UUID.fromString(cardIdStr)).toOption, indexStr.toIntOption) match {
          case (Some(cardId), Some(index)) =>
            BotCommand.SelectCard(cardId, index)
          case _ =>
            BotCommand.Unknown
        }  
      case TelegramCommands.SpreadPublish :: scheduledAtStr :: Nil =>
        Try(Instant.ofEpochSecond(scheduledAtStr.toLong)).toOption match {
          case Some(scheduledAt) => BotCommand.PublishSpread(scheduledAt)
          case None => BotCommand.Unknown
        }
      case List(_, _*) | Nil =>
        BotCommand.Unknown
    }
}
