package bot.application.handlers.telegram

import bot.application.commands.*
import bot.application.commands.telegram.SchedulerCommands
import bot.infrastructure.services.calendar.CalendarService
import shared.infrastructure.services.common.DateTimeService
import zio.{UIO, ZIO}

import java.time.*
import scala.util.Try

object TelegramSchedulerParser {
  def handle(command: String): UIO[BotCommand] =
    command.split("\\s+").toList match {
        case SchedulerCommands.SelectMonth :: monthStr :: Nil =>
          for {
            today <- DateTimeService.currentLocalDate()
            command <- Try(YearMonth.parse(monthStr)).toOption match {
              case Some(month) if CalendarService.canNavigatePrevMonth(today, month) =>
                ZIO.succeed(ScheduleCommand.SelectMonth(month))
              case Some(month) =>
                ZIO.logWarning(s"Blocked navigation before current month: $month").as(BotCommand.Unknown)
              case None =>
                ZIO.succeed(BotCommand.Unknown)
            }
          } yield command
        case SchedulerCommands.SelectDate :: dateStr :: Nil =>
          for {
            today <- DateTimeService.currentLocalDate()
            command <- Try(LocalDate.parse(dateStr)).toOption match {
                case Some(date) if CalendarService.canNavigatePrevDay(today, date) =>
                  ZIO.succeed(ScheduleCommand.SelectDate(date))
                case Some(date) =>
                  ZIO.logWarning(s"Blocked navigation before current date: $date").as(BotCommand.Unknown)
                case None =>
                  ZIO.succeed(BotCommand.Unknown)
              }
          } yield command
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
