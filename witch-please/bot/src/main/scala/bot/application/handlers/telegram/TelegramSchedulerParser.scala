package bot.application.handlers.telegram

import bot.application.commands.*
import bot.application.commands.telegram.SchedulerCommands
import bot.application.handlers.telegram.markup.SchedulerGuard
import zio.{UIO, ZIO}

import java.time.*
import scala.util.Try

object TelegramSchedulerParser {
  def handle(command: String): UIO[BotCommand] =
    command.split("\\s+").toList match {
        case SchedulerCommands.SelectMonth :: monthStr :: Nil =>
          Try(YearMonth.parse(monthStr)).toOption match {
            case Some(month) =>
              SchedulerGuard.canNavigatePrevMonth(month).flatMap {
                case true =>
                  ZIO.succeed(ScheduleCommand.SelectMonth(month))
                case false =>
                  ZIO.logWarning(s"Blocked navigation before current month: $month")
                    .as(BotCommand.Unknown)
              }
            case None =>
              ZIO.succeed(BotCommand.Unknown)
          }
        case SchedulerCommands.SelectDate :: dateStr :: Nil =>
          Try(LocalDate.parse(dateStr)).toOption match {
            case Some(date) =>
              SchedulerGuard.canNavigatePrevDay(date).flatMap {
                case true =>
                  ZIO.succeed(ScheduleCommand.SelectDate(date))
                case false =>
                  ZIO.logWarning(s"Blocked navigation before current date: $date")
                    .as(BotCommand.Unknown)
              }
            case None =>
              ZIO.succeed(BotCommand.Unknown)
          }
        case SchedulerCommands.SelectTime :: timeStr :: Nil =>
          Try(LocalTime.parse(timeStr)).toOption match {
            case Some (time) =>
              ZIO.succeed(ScheduleCommand.SelectTime(time))
            case _ =>
              ZIO.succeed(BotCommand.Unknown)
          }
        case List(SchedulerCommands.Confirm) =>
          ZIO.succeed(ScheduleCommand.Confirm)
        case _ =>
          ZIO.succeed(BotCommand.Unknown)
    }
}
