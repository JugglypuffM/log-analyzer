package logs.analyze

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import domain.logs.{LogRecord, Statistics}
import fs2.Stream
import org.http4s.*
import org.http4s.Status.*
import org.scalatest.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

import java.net.InetAddress
import java.time.{ZoneOffset, ZonedDateTime}

class LogAnalyzerSpec extends AsyncWordSpec with AsyncIOSpec with Matchers {

  "LogAnalyzer" should {

    "collect statistics correctly from a stream of log records" in {
      val logs = Stream(
        LogRecord(
          InetAddress.getByName("126.0.0.1"),
          "user1",
          ZonedDateTime.of(2024, 11, 1, 11, 0, 0, 0, ZoneOffset.UTC),
          Method.GET,
          "/home",
          HttpVersion.`HTTP/1.1`,
          Ok,
          200,
          "referer1",
          "Mozilla/4.0"
        ),
        LogRecord(
          InetAddress.getByName("127.0.0.1"),
          "user2",
          ZonedDateTime.of(2024, 11, 1, 12, 0, 0, 0, ZoneOffset.UTC),
          Method.POST,
          "/about",
          HttpVersion.`HTTP/1.1`,
          NotFound,
          300,
          "referer2",
          "Mozilla/3.0"
        ),
        LogRecord(
          InetAddress.getByName("128.0.0.1"),
          "user1",
          ZonedDateTime.of(2024, 11, 1, 13, 0, 0, 0, ZoneOffset.UTC),
          Method.GET,
          "/home",
          HttpVersion.`HTTP/1.1`,
          Ok,
          150,
          "referer3",
          "Mozilla/5.0"
        )
      )

      val statistics: IO[Statistics] = LogAnalyzer.collectStatistics(logs)

      statistics.map { result =>
        result shouldBe Statistics(
          numberOfRequests = 3,
          resourcesFrequency = Map("/about" -> 1, "/home" -> 2),
          codesFrequency = Map(NotFound -> 1, Ok -> 2),
          addressFrequency = Map(
            InetAddress.getByName("126.0.0.1") -> 1,
            InetAddress.getByName("127.0.0.1") -> 1,
            InetAddress.getByName("128.0.0.1") -> 1
          ),
          userAgentsFrequency =
            Map("Mozilla/4.0" -> 1, "Mozilla/3.0" -> 1, "Mozilla/5.0" -> 1),
          responseByteSizes = List(150, 300, 200),
          Some(ZonedDateTime.of(2024, 11, 1, 11, 0, 0, 0, ZoneOffset.UTC)),
          Some(ZonedDateTime.of(2024, 11, 1, 13, 0, 0, 0, ZoneOffset.UTC))
        )
      }
    }

    "handle empty log stream correctly" in {
      val emptyLogs = Stream.empty[IO]

      val statistics: IO[Statistics] = LogAnalyzer.collectStatistics(emptyLogs)

      statistics.map { result =>
        result shouldBe Statistics(
          numberOfRequests = 0,
          resourcesFrequency = Map.empty,
          codesFrequency = Map.empty,
          addressFrequency = Map.empty,
          userAgentsFrequency = Map.empty,
          responseByteSizes = List.empty,
          None,
          None
        )
      }
    }

    "handle logs with identical records correctly" in {
      val logs = Stream(
        LogRecord(
          InetAddress.getByName("127.0.0.1"),
          "user1",
          ZonedDateTime.of(2024, 11, 1, 11, 0, 0, 0, ZoneOffset.UTC),
          Method.GET,
          "/home",
          HttpVersion.`HTTP/1.1`,
          Ok,
          200,
          "referer1",
          "Mozilla/5.0"
        ),
        LogRecord(
          InetAddress.getByName("127.0.0.1"),
          "user1",
          ZonedDateTime.of(2024, 11, 1, 11, 0, 0, 0, ZoneOffset.UTC),
          Method.GET,
          "/home",
          HttpVersion.`HTTP/1.1`,
          Ok,
          200,
          "referer2",
          "Mozilla/5.0"
        ),
        LogRecord(
          InetAddress.getByName("127.0.0.1"),
          "user1",
          ZonedDateTime.of(2024, 11, 1, 12, 0, 0, 0, ZoneOffset.UTC),
          Method.GET,
          "/home",
          HttpVersion.`HTTP/1.1`,
          Ok,
          200,
          "referer3",
          "Mozilla/5.0"
        )
      )

      val statistics: IO[Statistics] = LogAnalyzer.collectStatistics(logs)

      statistics.map { result =>
        result shouldBe Statistics(
          numberOfRequests = 3,
          resourcesFrequency = Map("/home" -> 3),
          codesFrequency = Map(Ok -> 3),
          addressFrequency = Map(InetAddress.getByName("127.0.0.1") -> 3),
          userAgentsFrequency = Map("Mozilla/5.0" -> 3),
          responseByteSizes = List(200, 200, 200),
          Some(ZonedDateTime.of(2024, 11, 1, 11, 0, 0, 0, ZoneOffset.UTC)),
          Some(ZonedDateTime.of(2024, 11, 1, 12, 0, 0, 0, ZoneOffset.UTC))
        )
      }
    }

  }
}
