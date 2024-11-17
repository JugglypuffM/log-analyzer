package logs.processing

import cats.Applicative
import cats.implicits.*
import domain.logs.LogRecord
import org.http4s.{HttpVersion, Method, Status}

import java.net.InetAddress
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object LogParser {
  private val logRegex =
    """(.+) - (\S+) \[(.+)] "(.+) (\S+) (.+)" (.+) (.+) "(\S+)" "(.*)"""".r
  private val dateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z")

  def parse[F[_]: Applicative](line: String): F[Either[Throwable, LogRecord]] =
    line match {
      case logRegex(
            rawAddress,
            user,
            rawTime,
            rawMethod,
            resource,
            rawVersion,
            rawStatus,
            rawBytesSent,
            referer,
            agent
          ) =>
        (for {
          address <- Either.catchNonFatal(InetAddress.getByName(rawAddress))
          time <- Either.catchNonFatal(
            ZonedDateTime.parse(rawTime, dateTimeFormatter)
          )
          method <- Method.fromString(rawMethod)
          protocol <- HttpVersion.fromString(rawVersion)
          rawStatusInt <- Either.catchNonFatal(rawStatus.toInt)
          status <- Status.fromInt(rawStatusInt)
          bytesSent <- Either.catchNonFatal(rawBytesSent.toInt)
        } yield LogRecord(
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
        )).pure[F]
      case _ => Left(MatchError(s"Failed to parse line")).pure[F]
    }

}
