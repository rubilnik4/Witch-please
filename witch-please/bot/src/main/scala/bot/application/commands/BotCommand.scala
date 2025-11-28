package bot.application.commands

import java.time.*
import java.util.UUID

sealed trait BotCommand
sealed trait AuthorCommand extends BotCommand
sealed trait ClientCommand extends BotCommand
sealed trait ScheduleCommand extends BotCommand

object BotCommand {
  case object Start extends BotCommand
  case object Noop extends BotCommand
  case object Help extends BotCommand
  case object Unknown extends BotCommand
}

object AuthorCommand {  
  case object Start extends AuthorCommand
  case object CreateSpread extends AuthorCommand
  final case class EditSpread(spreadId: UUID) extends AuthorCommand
  final case class SelectSpread(spreadId: UUID, cardCount: Int) extends AuthorCommand
  final case class SelectSpreadCards(spreadId: UUID) extends AuthorCommand
  final case class PublishSpread(spreadId: UUID) extends AuthorCommand
  final case class DeleteSpread(spreadId: UUID) extends AuthorCommand
  final case class CreateCard(index: Int) extends AuthorCommand
}

object ClientCommand {
  case object Start extends ClientCommand
  final case class SelectAuthor(authorId: UUID) extends ClientCommand
}

object ScheduleCommand {
  final case class SelectMonth(month: YearMonth) extends ScheduleCommand
  final case class SelectDate(date: LocalDate) extends ScheduleCommand
  final case class SelectTimePage(page: Int) extends ScheduleCommand
  final case class SelectTime(time: LocalTime) extends ScheduleCommand
  final case class SelectCardOfDay(delay: Duration) extends ScheduleCommand
  object Confirm extends ScheduleCommand
}