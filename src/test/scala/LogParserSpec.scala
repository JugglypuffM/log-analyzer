import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import domain.LogRecord
import org.http4s.{HttpVersion, Method, Status}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

import java.net.InetAddress
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class LogParserSpec extends AsyncWordSpec with AsyncIOSpec with Matchers {
  "LogParser" should {

    "correctly parse a valid log line" in {
      val logLine =
        """192.168.0.1 - user1 [01/Jan/2023:12:34:56 +0000] "GET /home HTTP/1.1" 200 512 "-" "Mozilla/5.0""""

      val expectedLogRecord = LogRecord(
        address = InetAddress.getByName("192.168.0.1"),
        user = "user1",
        time = ZonedDateTime.parse(
          "01/Jan/2023:12:34:56 +0000",
          DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z")
        ),
        method = Method.GET,
        resource = "/home",
        protocol = HttpVersion.`HTTP/1.1`,
        status = Status.Ok,
        bytesSent = 512,
        referer = "-",
        userAgent = "Mozilla/5.0"
      )

      LogParser
        .parse[IO](logLine)
        .asserting(_ shouldBe Right(expectedLogRecord))
    }

    "return an error for an invalid log line" in {
      val logLine = """invalid log line"""

      LogParser.parse[IO](logLine).asserting { result =>
        result shouldBe a[Left[_, LogRecord]]
      }
    }

    "return an error for a log line with missing fields" in {
      val logLine =
        """192.168.0.1 - user1 [01/Jan/2023:12:34:56 +0000] "GET /home HTTP/1.1" 200"""

      LogParser.parse[IO](logLine).asserting { result =>
        result shouldBe a[Left[_, LogRecord]]
      }
    }

    "return an error for a log line with incorrect address format" in {
      val logLine =
        """600.168.0.1 - user1 [01/Jan/2023:12:34:56 +0000] "GET /home HTTP/1.1" 200 512 "-" "Mozilla/5.0""""

      LogParser.parse[IO](logLine).asserting { result =>
        result shouldBe a[Left[_, LogRecord]]
      }
    }

    "return an error for a log line with incorrect date format" in {
      val logLine =
        """192.168.0.1 - user1 [01-01-2023:12:34:56] "GET /home HTTP/1.1" 200 512 "-" "Mozilla/5.0""""

      LogParser.parse[IO](logLine).asserting { result =>
        result shouldBe a[Left[_, LogRecord]]
      }
    }

    "return an error for a log line with incorrect method name" in {
      val logLine =
        """192.168.0.1 - user1 [01/Jan/2023:12:34:56 +0000] "n\///\.k+c /home HTTP/1.1" 200 512 "-" "Mozilla/5.0""""

      LogParser.parse[IO](logLine).asserting { result =>
        result shouldBe a[Left[_, LogRecord]]
      }
    }

    "return an error for a log line with incorrect http version" in {
      val logLine =
        """192.168.0.1 - user1 [01/Jan/2023:12:34:56 +0000] "GET /home HTTP/322" 200 512 "-" "Mozilla/5.0""""

      LogParser.parse[IO](logLine).asserting { result =>
        result shouldBe a[Left[_, LogRecord]]
      }
    }

    "return an error for a log line with incorrect status code" in {
      val logLine =
        """192.168.0.1 - user1 [01/Jan/2023:12:34:56 +0000] "GET /home HTTP/1.1" 999 512 "-" "Mozilla/5.0""""

      LogParser.parse[IO](logLine).asserting { result =>
        result shouldBe a[Left[_, LogRecord]]
      }
    }

    "return an error for a log line with invalid status code" in {
      val logLine =
        """192.168.0.1 - user1 [01/Jan/2023:12:34:56 +0000] "GET /home HTTP/1.1" abc 512 "-" "Mozilla/5.0""""

      LogParser.parse[IO](logLine).asserting { result =>
        result shouldBe a[Left[_, LogRecord]]
      }
    }

    "return an error for a log line with invalid bytes value" in {
      val incorrectDateLogLine =
        """192.168.0.1 - user1 [01/Jan/2023:12:34:56 +0000] "GET /home HTTP/1.1" 200 abc "-" "Mozilla/5.0""""

      LogParser.parse[IO](incorrectDateLogLine).asserting { result =>
        result shouldBe a[Left[_, LogRecord]]
      }
    }
  }
}
