package bot.application.handlers.telegram.parsers

import bot.application.commands.{BotCommand, ClientCommand}
import bot.application.commands.telegram.TelegramCommands

import java.util.UUID
import scala.util.Try

object TelegramPhotoParser {
  def handle(command: String): BotCommand =
    command.split("\\s+").toList match {
      case TelegramCommands.PhotoShow :: photoIdStr :: Nil =>
        Try(UUID.fromString(photoIdStr)).toOption match {
          case Some(photoId) =>
            BotCommand.ShowPhoto(photoId)
          case _ =>
            BotCommand.Unknown
        }
      case _ =>
        BotCommand.Unknown
    }
}
