package bot.domain.models.calendar

import java.time.LocalDateTime

enum CalendarTimeStep(val minutes: Int):
  case M30 extends CalendarTimeStep(30)
  case M60 extends CalendarTimeStep(60)
  case M120 extends CalendarTimeStep(120)
  case M240 extends CalendarTimeStep(240)

object CalendarTimeStep:
  val all: List[CalendarTimeStep] = List(M30, M60, M120, M240)

  def fromMinutes(minutes: Int): Option[CalendarTimeStep] =
    all.find(_.minutes == minutes)

  def nearest(minutes: Int): CalendarTimeStep =
    all.minBy(timeStep => math.abs(timeStep.minutes - minutes))

  def alignUp(dateTime: LocalDateTime, timeStep: CalendarTimeStep): LocalDateTime = {
    val base = dateTime.withSecond(0).withNano(0)
    val add = (timeStep.minutes - (base.getMinute % timeStep.minutes)) % timeStep.minutes
    base.plusMinutes(add.toLong)
  }

  def alignDown(dateTime: LocalDateTime, timeStep: CalendarTimeStep): LocalDateTime = {
    val base = dateTime.withSecond(0).withNano(0)
    val sub = base.getMinute % timeStep.minutes
    base.minusMinutes(sub.toLong)
  }