package domain

import java.time.ZonedDateTime

case class LogRecord(
    address: String,
    user: String,
    time: ZonedDateTime,
    method: String,
    resource: String,
    protocol: String,
    status: Int,
    bytesSent: Int,
    referer: String,
    userAgent: String
)
