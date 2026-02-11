package bot.application.commands

import java.time.*
import java.util.UUID

sealed trait ClientCommand extends BotCommand

object ClientCommand {
  case object Start extends ClientCommand
  final case class SelectAuthor(authorId: UUID) extends ClientCommand
}