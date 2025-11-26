package bot.application.handlers.telegram

import bot.application.commands.*
import bot.application.commands.telegram.SchedulerCommands
import shared.infrastructure.services.common.DateTimeService
import zio.{UIO, ZIO}

import java.time.*
import scala.util.Try

object TelegramSchedulerParser {
  def handle(command: String): BotCommand =
    command.split("\\s+").toList match {
        case SchedulerCommands.SelectMonth :: monthStr :: Nil =>
          Try(YearMonth.parse(monthStr)).toOption match {
            case Some(month) =>
              ScheduleCommand.SelectMonth(month)
            case None =>
              BotCommand.Unknown
          }      
        case SchedulerCommands.SelectDate :: dateStr :: Nil =>
          Try(LocalDate.parse(dateStr)).toOption match {              
            case Some(date) =>
              ScheduleCommand.SelectDate(date)
            case None =>
              BotCommand.Unknown
          }
        case SchedulerCommands.SelectTimePage :: pageStr :: Nil =>
          pageStr.toIntOption match {
            case Some(page) =>
              ScheduleCommand.SelectTimePage(page)
            case None =>
              BotCommand.Unknown
          }
        case SchedulerCommands.SelectTime :: timeStr :: Nil =>
          Try(LocalTime.parse(timeStr)).toOption match {             
            case Some(time) =>
              ScheduleCommand.SelectTime(time)
            case None =>
              BotCommand.Unknown
          }
        case List(SchedulerCommands.Confirm) =>
          ScheduleCommand.Confirm
        case _ =>
          BotCommand.Unknown
    }
}
