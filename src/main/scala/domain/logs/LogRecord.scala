package domain.logs

import org.http4s.{HttpVersion, Method, Status}

import java.net.InetAddress
import java.time.ZonedDateTime

case class LogRecord(
    address: InetAddress,
    user: String,
    time: ZonedDateTime,
    method: Method,
    resource: String,
    protocol: HttpVersion,
    status: Status,
    bytesSent: Int,
    referer: String,
    userAgent: String
)
