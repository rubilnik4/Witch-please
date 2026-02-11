package bot.application.commands

import java.time.*
import java.util.UUID

sealed trait AuthorCommand extends BotCommand

object AuthorCommand {  
  case object Start extends AuthorCommand
  case object CreateChannel extends AuthorCommand
  final case class EditChannel(userChannelId: UUID) extends AuthorCommand
  case object CreateSpread extends AuthorCommand
  final case class EditSpread(spreadId: UUID) extends AuthorCommand
  final case class CloneSpread(spreadId: UUID) extends AuthorCommand
  case object SelectSpreads extends AuthorCommand
  final case class SelectSpread(spreadId: UUID) extends AuthorCommand
  final case class PublishSpread(spreadId: UUID) extends AuthorCommand
  final case class DeleteSpread(spreadId: UUID) extends AuthorCommand
  final case class CreateCard(position: Int) extends AuthorCommand
  final case class EditCard(cardId: UUID) extends AuthorCommand
  final case class DeleteCard(cardId: UUID) extends AuthorCommand
  final case class SelectCards(spreadId: UUID) extends AuthorCommand
  final case class SelectCard(cardId: UUID) extends AuthorCommand
  case object CreateCardOfDay extends AuthorCommand
  final case class EditCardOfDay(cardOfDayId: UUID) extends AuthorCommand
  final case class DeleteCardOfDay(cardOfDayId: UUID) extends AuthorCommand
  final case class SelectCardOfDay(spreadId: UUID) extends AuthorCommand
}