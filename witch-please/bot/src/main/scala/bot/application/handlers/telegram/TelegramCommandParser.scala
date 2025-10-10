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
      case List(TelegramCommands.ProjectsGet) =>
        BotCommand.GetProjects
      case List(TelegramCommands.SpreadCreate) =>
        BotCommand.CreateSpread
      case TelegramCommands.SpreadsGet :: projectIdStr :: Nil =>
        Try(UUID.fromString(projectIdStr)).toOption match {
          case Some(projectId) =>
            BotCommand.GetSpreads(projectId)
          case _ =>
            BotCommand.Unknown
        }
      case TelegramCommands.CardCreate :: indexStr :: Nil =>
        indexStr.toIntOption match {
          case Some(index) =>
            BotCommand.CreateCard(index)
          case _ =>
            BotCommand.Unknown
        }
      case TelegramCommands.CardsGet :: spreadIdStr :: Nil =>
        Try(UUID.fromString(spreadIdStr)).toOption match {
          case Some(spreadId) =>
            BotCommand.GetCards(spreadId)
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
