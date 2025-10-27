package bot.application.handlers.telegram.markup

import bot.application.commands.telegram.SchedulerCommands
import shared.api.dto.telegram.*
import zio.{UIO, ZIO}

import java.time.YearMonth

object SchedulerMarkup {
  def monthKeyboard(month: YearMonth = YearMonth.now()): UIO[List[List[TelegramInlineKeyboardButton]]] =
    for {
      header <- monthHeader(month)
      days <- daysGrid(month)
  } yield header :: days

  private def monthHeader(month: YearMonth): UIO[List[TelegramInlineKeyboardButton]] = {
    val prevMonth = month.minusMonths(1)
    val nextMonth = month.plusMonths(1)
    for {
      allowPrev <- SchedulerGuard.canNavigatePrevMonth(prevMonth)
    } yield {
      val prevButton =
        if (allowPrev) TelegramInlineKeyboardButton("◀️", Some(SchedulerCommands.selectMonth(prevMonth)))
        else TelegramInlineKeyboardButton(" ", Some(TelegramKeyboard.StubCommand))

      val nextButton = TelegramInlineKeyboardButton("▶️", Some(SchedulerCommands.selectMonth(nextMonth)))
      val title = TelegramInlineKeyboardButton(s"${month.getMonth} ${month.getYear}", Some(TelegramKeyboard.StubCommand))

      List(prevButton, title, nextButton)
    }
  }

  private def daysGrid(month: YearMonth): UIO[List[List[TelegramInlineKeyboardButton]]] =
    for {
      buttons <- ZIO.foreach(1 to month.lengthOfMonth) { day =>
        val date = month.atDay(day)
        SchedulerGuard.canNavigatePrevDay(date).map { allowed =>
          if (allowed) Some(TelegramInlineKeyboardButton(f"$day%2d", Some(SchedulerCommands.selectDate(date))))
          else None
        }
      }.map(_.flatten)
    } yield buttons.grouped(7).map(_.toList).toList
}
