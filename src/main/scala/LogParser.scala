import domain.LogRecord

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

object LogParser {
  private val logRegex =
    """(\S+) - (\S+) \[(.+)] "(\S+) (\S+) (\S+)" (\d{3}) (\d+) "(\S+)" "(.*)"""".r
  private val dateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z")

  def parse(line: String): Option[LogRecord] = line match {
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
      Try(
        Option(
          LogRecord(
            address,
            user,
            ZonedDateTime.parse(time, dateTimeFormatter),
            method,
            resource,
            protocol,
            status.toInt,
            bytesSent.toInt,
            referer,
            agent
          )
        )
      ).getOrElse(None)
    case _ => None
  }

}
