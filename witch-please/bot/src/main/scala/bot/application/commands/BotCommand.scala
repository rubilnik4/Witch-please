package bot.application.commands

import java.time.Instant
import java.util.UUID

sealed trait BotCommand

object BotCommand {
  case object Start extends BotCommand
  case object CreateProject extends BotCommand
  case object GetProjects extends BotCommand
  final case class CreateSpread(title: String, cardCount: Int) extends BotCommand
  final case class GetSpreads(projectId: UUID) extends BotCommand
  final case class CreateCard(description: String, index: Int) extends BotCommand
  final case class PublishSpread(scheduledAt: Instant) extends BotCommand
  case object Help extends BotCommand
  case object Unknown extends BotCommand
}
