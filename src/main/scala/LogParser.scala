import cats.MonadThrow
import cats.implicits.*
import domain.LogRecord

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

object LogParser {
  private val logRegex =
    """(\S+) - (\S+) \[(.+)] "(\S+) (\S+) (\S+)" (\d{3}) (\d+) "(\S+)" "(.*)"""".r
  private val dateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z")

  def parse[F[_]: MonadThrow](line: String): F[LogRecord] = line match {
    case logRegex(
          address,
          user,
          time,
          method,
          resource,
          protocol,
          status,
          bytesSent,
          referer,
          agent
        ) =>
      for {
        date <- Try(ZonedDateTime.parse(time, dateTimeFormatter)).liftTo[F]
      } yield LogRecord(
        address,
        user,
        date,
        method,
        resource,
        protocol,
        status.toInt,
        bytesSent.toInt,
        referer,
        agent
      )
    case _ => MonadThrow[F].raiseError(MatchError(s"Failed to parse line '$line'"))
  }

}
