package logs.analyze

import cats.effect.Async
import cats.implicits.catsSyntaxSemigroup
import domain.{LogRecord, Statistics}
import fs2.Stream

object LogAnalyzer {
  def collectStatistics[F[_]: Async](
      logs: Stream[F, LogRecord]
  ): F[Statistics] = {
    logs.compile.fold(Statistics(0, Map.empty, Map.empty, Map.empty, Map.empty, List.empty)) {
      case (acc, log) =>
        acc.copy(
          numberOfRequests = acc.numberOfRequests + 1,
          resourcesFrequency =
            acc.resourcesFrequency |+| Map(log.resource -> 1),
          addressFrequency = acc.addressFrequency |+| Map(log.address -> 1),
          userAgentsFrequency = acc.userAgentsFrequency |+| Map(log.userAgent -> 1),
          codesFrequency = acc.codesFrequency |+| Map(log.status -> 1),
          responseByteSizes = log.bytesSent :: acc.responseByteSizes
        )
    }
  }
}
