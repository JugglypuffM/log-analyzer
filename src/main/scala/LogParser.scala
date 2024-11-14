import cats.MonadThrow
import cats.implicits.*
import domain.LogRecord
import org.http4s.{HttpVersion, Method, Status}

import java.net.InetAddress
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

object LogParser {
  private val logRegex =
    """(.+) - (\S+) \[(.+)] "(.+) (\S+) (.+)" (.+) (.+) "(\S+)" "(.*)"""".r
  private val dateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z")

  def parse[F[_]: MonadThrow](line: String): F[LogRecord] = line match {
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
      for {
        address <- Try(InetAddress.getByName(rawAddress)).liftTo[F]
        time <- Try(ZonedDateTime.parse(rawTime, dateTimeFormatter)).liftTo[F]
        method <- Method.fromString(rawMethod).liftTo
        protocol <- HttpVersion.fromString(rawVersion).liftTo
        status <- Status.fromInt(rawStatus.toInt).liftTo
        bytesSent <- Try(rawBytesSent.toInt).liftTo[F]
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
      )
    case _ =>
      MonadThrow[F].raiseError(MatchError(s"Failed to parse line"))
  }

}
