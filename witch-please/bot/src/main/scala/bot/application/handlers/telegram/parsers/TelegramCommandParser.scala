package bot.application.handlers.telegram.parsers

import bot.application.commands.telegram.*
import bot.application.commands.{BotCommand, AuthorCommand}
import bot.application.handlers.telegram.parsers.*

import java.util.UUID
import scala.util.Try

object TelegramCommandParser {
  def handle(command: String): BotCommand =
    command.trim match {
      case command if command.startsWith(AuthorCommands.Prefix) =>
        TelegramAuthorParser.handle(command)
      case command if command.startsWith(ClientCommands.Prefix) =>
        TelegramClientParser.handle(command)  
      case command if command.startsWith(SchedulerCommands.Prefix) =>
        TelegramSchedulerParser.handle(command)
      case command if command.startsWith(TelegramCommands.StubCommand) =>
        BotCommand.Noop
      case command if command.startsWith(TelegramCommands.Help) =>
        BotCommand.Help
      case _ =>
        BotCommand.Unknown
    }    
}
