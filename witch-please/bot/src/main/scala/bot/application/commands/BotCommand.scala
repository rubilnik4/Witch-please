package bot.application.commands

import java.time.Instant
import java.util.UUID

sealed trait BotCommand

object BotCommand {
  case object Start extends BotCommand
  case class CreateUser(name: String) extends BotCommand
  case class CreateProject(name: String) extends BotCommand
  case class CreateSpread(title: String, cardCount: Int) extends BotCommand
  case class CreateCard(description: String, index: Int) extends BotCommand
  case class PublishSpread(scheduledAt: Instant) extends BotCommand
  case object Help extends BotCommand
  case object Unknown extends BotCommand
}
