package bot.application.commands

import java.util.UUID

sealed trait BotCommand

object BotCommand {
  case object Start extends BotCommand
  case class CreateUser(name: String) extends BotCommand
  case class CreateProject(name: String) extends BotCommand
  case class CreateSpread(title: String, cardCount: int) extends BotCommand
  case class CreateCard(index: Int, name: String) extends BotCommand
  case class ConfirmSpread(spreadId: UUID) extends BotCommand
  case object Help extends BotCommand
  case object Unknown extends BotCommand
}
