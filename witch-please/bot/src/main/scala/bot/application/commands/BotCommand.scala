package bot.application.commands

import java.time.Instant
import java.util.UUID

sealed trait BotCommand

object BotCommand {
  case object Start extends BotCommand
  case object CreateProject extends BotCommand
  final case class SelectProject(projectId: UUID) extends BotCommand
  case object CreateSpread extends BotCommand
  final case class SelectSpread(spreadId: UUID, cardCount: Int) extends BotCommand
  final case class CreateCard(index: Int) extends BotCommand
  final case class SelectCard(cardId: UUID, index: Int) extends BotCommand
  final case class PublishSpread(scheduledAt: Instant) extends BotCommand
  case object Help extends BotCommand
  case object Unknown extends BotCommand
}
