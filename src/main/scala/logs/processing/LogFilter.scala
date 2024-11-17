package logs.processing

import domain.{Config, LogRecord}
import fs2.Stream
import org.http4s.{HttpVersion, Method, Status}

import java.net.InetAddress
import java.time.{LocalDate, ZonedDateTime}
import scala.util.Try

object LogFilter {
  def filterWithConfig[F[_]](
      config: Config
  )(stream: Stream[F, LogRecord]): Stream[F, LogRecord] =
    stream.filter(log =>
      matchesTime(config.from, config.to, log.time) &&
        matchesField(config.filterField, config.filterValue, log)
    )

  def matchesTime(
      maybeFrom: Option[LocalDate],
      maybeTo: Option[LocalDate],
      logZonedTime: ZonedDateTime
  ): Boolean =
    val logTime = logZonedTime.toLocalDate
    maybeFrom.forall(from => logTime.isAfter(from) || logTime.isEqual(from)) &&
      maybeTo.forall(to => logTime.isBefore(to) || logTime.isEqual(to))

  def matchesField(
      maybeField: Option[String],
      maybeValue: Option[String],
      record: LogRecord
  ): Boolean = (maybeField, maybeValue) match
    case (Some(field), Some(value)) =>
      field match
        case "address" =>
          Try(InetAddress.getByName(value) == record.address).getOrElse(false)
        case "user" => value.strip() == record.user
        case "method" =>
          Try(Method.fromString(value.toUpperCase) == Right(record.method)).getOrElse(false)
        case "resource" => value.strip() == record.resource
        case "protocol" =>
          Try(HttpVersion.fromString(value) == Right(record.protocol))
            .getOrElse(false)
        case "status" =>
          Try(Status.fromInt(value.toInt) == Right(record.status))
            .getOrElse(false)
        case "bytessent" =>
          Try(value.toInt == record.bytesSent).getOrElse(false)
        case "referer"   => value.strip() == record.referer
        case "useragent" => record.userAgent.contains(value.strip())
        case _           => false
    case _ => true
}
