package bot.application.handlers.telegram

import bot.application.commands.BotCommand

import java.time.Instant
import java.util.UUID
import scala.util.Try

object TelegramCommandParser {
  def handle(command: String): BotCommand = {
    val parts = command.trim.split("\\s+").toList

    parts match {
      case List("start") =>
        BotCommand.Start
      case List("help") =>
        BotCommand.Help
      case "project_create" :: nameParts if nameParts.nonEmpty =>
        BotCommand.CreateProject(nameParts.mkString(" "))
      case "spread_create" :: cardCountStr :: nameParts if nameParts.nonEmpty =>
        Try(cardCountStr.toInt).toOption match {
          case Some(cardCount) if nameParts.nonEmpty =>
            BotCommand.CreateSpread(nameParts.mkString(" "), cardCount)
          case _ => BotCommand.Unknown
        }  
      case "card_create" :: indexStr :: nameParts =>
        Try(indexStr.toInt).toOption match {
          case Some(index) if nameParts.nonEmpty =>
            BotCommand.CreateCard(nameParts.mkString(" "), index)
          case _ => BotCommand.Unknown
        }
      case "spread_confirm" :: scheduledAtStr :: Nil =>
        Try(Instant.ofEpochSecond(scheduledAtStr.toLong)).toOption match {
          case Some(scheduledAt) => BotCommand.PublishSpread(scheduledAt)
          case None => BotCommand.Unknown
        }
      case _ =>
        BotCommand.Unknown
    }
  }
}
