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
      case TelegramCommands.SpreadCreate :: cardCountStr :: nameParts if nameParts.nonEmpty =>
        Try(cardCountStr.toInt).toOption match {
          case Some(cardCount) if nameParts.nonEmpty =>
            BotCommand.CreateSpread(nameParts.mkString(" "), cardCount)
          case _ => BotCommand.Unknown
        }
      case TelegramCommands.SpreadsGet :: spreadIdStr :: Nil =>
        Try(UUID.fromString(spreadIdStr)).toOption match {
          case Some(spreadId) =>
            BotCommand.GetSpreads(spreadId)
          case _ =>
            BotCommand.Unknown
        }
      case TelegramCommands.CardCreate :: indexStr :: nameParts =>
        Try(indexStr.toInt).toOption match {
          case Some(index) if nameParts.nonEmpty =>
            BotCommand.CreateCard(nameParts.mkString(" "), index)
          case _ => BotCommand.Unknown
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
