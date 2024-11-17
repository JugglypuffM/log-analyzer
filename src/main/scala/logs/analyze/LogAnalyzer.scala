package logs.analyze

import cats.effect.Async
import cats.implicits.catsSyntaxSemigroup
import domain.logs.{LogRecord, Statistics}
import fs2.Stream

object LogAnalyzer {
  def collectStatistics[F[_]: Async](
      logs: Stream[F, LogRecord]
  ): F[Statistics] = {
    logs.compile.fold(
      Statistics(
        0,
        Map.empty,
        Map.empty,
        Map.empty,
        Map.empty,
        List.empty,
        None,
        None
      )
    ) { case (acc, log) =>
      acc.copy(
        numberOfRequests = acc.numberOfRequests + 1,
        resourcesFrequency = acc.resourcesFrequency |+| Map(log.resource -> 1),
        addressFrequency = acc.addressFrequency |+| Map(log.address -> 1),
        userAgentsFrequency =
          acc.userAgentsFrequency |+| Map(log.userAgent -> 1),
        codesFrequency = acc.codesFrequency |+| Map(log.status -> 1),
        responseByteSizes = log.bytesSent :: acc.responseByteSizes,
        startDate = acc.startDate match
          case Some(date) =>
            if (date.isAfter(log.time)) Some(log.time) else acc.startDate
          case None => Some(log.time),
        endDate = acc.endDate match
          case Some(date) =>
            if (date.isBefore(log.time)) Some(log.time) else acc.startDate
          case None => Some(log.time)
      )
    }
  }
}
