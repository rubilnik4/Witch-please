package shared.infrastructure.services.common

import zio.{Clock, UIO}

import java.time.*

object DateTimeService {
  final val Zone = ZoneId.of("Europe/Moscow")
  
  def getDateTimeNow: UIO[Instant] = 
    Clock.instant

  def getOffset: UIO[ZoneOffset] =
    getDateTimeNow.map(Zone.getRules.getOffset)   

  def currentLocalDateTime(zone: ZoneId = Zone): UIO[LocalDateTime] =
    getDateTimeNow.map(i => ZonedDateTime.ofInstant(i, zone).toLocalDateTime)  
    
  def currentLocalDate(zone: ZoneId = Zone): UIO[LocalDate] =
    getDateTimeNow.map(i => ZonedDateTime.ofInstant(i, zone).toLocalDate)

  def currentYearMonth(zone: ZoneId = Zone): UIO[YearMonth] =
    getDateTimeNow.map(i => YearMonth.from(ZonedDateTime.ofInstant(i, zone)))
}