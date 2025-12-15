package shared.infrastructure.services.common

import zio.{Clock, UIO}

import java.time.*

object DateTimeService {
  final val Zone = ZoneId.of("Europe/Moscow")
  
  def getDateTimeNow: UIO[Instant] = 
    Clock.instant

  def getOffset: UIO[ZoneOffset] =
    getDateTimeNow.map(Zone.getRules.getOffset)

  def getLocalTime(dateTime: Instant, zone: ZoneId = Zone): LocalDateTime =
    ZonedDateTime.ofInstant(dateTime, zone).toLocalDateTime

  def currentLocalDateTime(zone: ZoneId = Zone): UIO[LocalDateTime] =
    getDateTimeNow.map(i => getLocalTime(i, zone))

  def getLocalDate(dateTime: Instant, zone: ZoneId = Zone): LocalDate =
    ZonedDateTime.ofInstant(dateTime, zone).toLocalDate
    
  def currentLocalDate(zone: ZoneId = Zone): UIO[LocalDate] =
    getDateTimeNow.map(i => getLocalDate(i, zone))

  def getYearMonth(dateTime: Instant, zone: ZoneId = Zone): YearMonth =
    YearMonth.from(ZonedDateTime.ofInstant(dateTime, zone))

  def currentYearMonth(zone: ZoneId = Zone): UIO[YearMonth] =
    getDateTimeNow.map(i => getYearMonth(i, zone))
}