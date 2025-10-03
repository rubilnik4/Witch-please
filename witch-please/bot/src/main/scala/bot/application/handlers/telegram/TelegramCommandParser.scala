package bot.application.handlers.telegram

import bot.application.commands.BotCommand
import bot.application.commands.telegram.TelegramCommands

import java.time.Instant
import java.util.UUID
import scala.util.Try

object TelegramCommandParser {
  def handle(command: String): BotCommand = {
    val parts = command.trim.split("\\s+").toList

    parts match {
      case List(TelegramCommands.Start) =>
        BotCommand.Start
      case List(TelegramCommands.Help) =>
        BotCommand.Help    
      case TelegramCommands.ProjectCreate :: nameParts if nameParts.nonEmpty =>
        BotCommand.CreateProject(nameParts.mkString(" "))
      case List(TelegramCommands.ProjectsGet) =>
        BotCommand.GetProjects
      case TelegramCommands.SpreadCreate :: cardCountStr :: nameParts if nameParts.nonEmpty =>
        Try(cardCountStr.toInt).toOption match {
          case Some(cardCount) if nameParts.nonEmpty =>
            BotCommand.CreateSpread(nameParts.mkString(" "), cardCount)
          case _ => BotCommand.Unknown
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
      case _ =>
        BotCommand.Unknown
    }
  }
}
