package logs.analyze

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import domain.{LogRecord, Statistics}
import fs2.Stream
import org.http4s.*
import org.http4s.Status.*
import org.scalatest.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

import java.net.InetAddress
import java.time.ZonedDateTime

class LogAnalyzerSpec extends AsyncWordSpec with AsyncIOSpec with Matchers {

  "LogAnalyzer" should {

    "collect statistics correctly from a stream of log records" in {
      val logs = Stream(
        LogRecord(
          InetAddress.getByName("127.0.0.1"),
          "user1",
          ZonedDateTime.now(),
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
          "user2",
          ZonedDateTime.now(),
          Method.POST,
          "/about",
          HttpVersion.`HTTP/1.1`,
          NotFound,
          300,
          "referer2",
          "Mozilla/5.0"
        ),
        LogRecord(
          InetAddress.getByName("127.0.0.1"),
          "user1",
          ZonedDateTime.now(),
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
          responseByteSizes = List(150, 300, 200)
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
          responseByteSizes = List.empty
        )
      }
    }

    "handle logs with identical records correctly" in {
      val logs = Stream(
        LogRecord(
          InetAddress.getByName("127.0.0.1"),
          "user1",
          ZonedDateTime.now(),
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
          ZonedDateTime.now(),
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
          ZonedDateTime.now(),
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
          responseByteSizes = List(200, 200, 200)
        )
      }
    }

  }
}
