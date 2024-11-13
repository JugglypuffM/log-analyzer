import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import domain.LogRecord
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class LogParserSpec extends AsyncWordSpec with AsyncIOSpec with Matchers {
  "LogParser" should {

    "correctly parse a valid log line" in {
      val logLine =
        """192.168.0.1 - user1 [01/Jan/2023:12:34:56 +0000] "GET /home HTTP/1.1" 200 512 "-" "Mozilla/5.0""""

      val expectedLogRecord = LogRecord(
        address = "192.168.0.1",
        user = "user1",
        time = ZonedDateTime.parse(
          "01/Jan/2023:12:34:56 +0000",
          DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z")
        ),
        method = "GET",
        resource = "/home",
        protocol = "HTTP/1.1",
        status = 200,
        bytesSent = 512,
        referer = "-",
        userAgent = "Mozilla/5.0"
      )

      LogParser.parse[IO](logLine).asserting(_ shouldBe expectedLogRecord)
    }

    "return an error for an invalid log line" in {
      val invalidLogLine = """invalid log line"""

      LogParser.parse[IO](invalidLogLine).attempt.asserting { result =>
        result shouldBe a [Left[MatchError, LogRecord]]
      }
    }

    "return an error for a log line with missing fields" in {
      val missingFieldsLogLine =
        """192.168.0.1 - user1 [01/Jan/2023:12:34:56 +0000] "GET /home HTTP/1.1" 200"""

      LogParser.parse[IO](missingFieldsLogLine).attempt.asserting { result =>
        result shouldBe a [Left[MatchError, LogRecord]]
      }
    }

    "return an error for a log line with incorrect date format" in {
      val incorrectDateLogLine =
        """192.168.0.1 - user1 [01-01-2023:12:34:56] "GET /home HTTP/1.1" 200 512 "-" "Mozilla/5.0""""

      LogParser.parse[IO](incorrectDateLogLine).attempt.asserting { result =>
        result shouldBe a [Left[MatchError, LogRecord]]
      }
    }
  }
}
