package bot.application.handlers.telegram.parsers

import bot.application.commands.telegram.ClientCommands
import bot.application.commands.{AuthorCommand, BotCommand, ClientCommand}

import java.util.UUID
import scala.util.Try

object TelegramClientParser {
  def handle(command: String): BotCommand =
    command.split("\\s+").toList match {
      case List(ClientCommands.Start) =>
        ClientCommand.Start
      case ClientCommands.AuthorSelect :: authorIdStr :: Nil =>
        Try(UUID.fromString(authorIdStr)).toOption match {
          case Some(authorId) =>
            ClientCommand.SelectAuthor(authorId)
          case _ =>
            BotCommand.Unknown
        }  
      case _ =>
        BotCommand.Unknown
    }
}
