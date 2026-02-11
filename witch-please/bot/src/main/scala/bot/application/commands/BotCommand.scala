package bot.application.commands

import java.util.UUID

trait BotCommand

object BotCommand {
  case object Start extends BotCommand
  final case class ShowPhoto(photoId: UUID) extends BotCommand
  case object Noop extends BotCommand
  case object Help extends BotCommand
  case object Unknown extends BotCommand
}
