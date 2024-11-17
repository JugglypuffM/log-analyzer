package logs.processing

import domain.LogRecord
import org.http4s.{HttpVersion, Method, Status}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.net.InetAddress
import java.time.{LocalDate, ZoneOffset, ZonedDateTime}

class LogFilterSpec extends AnyWordSpec with Matchers {
  "LogFilter.matchesTime" should {

    "return true if the log time is after or equal to from time and before or equal to to time" in {
      val fromTime = LocalDate.of(2024, 11, 1)
      val toTime = LocalDate.of(2024, 11, 1)
      val logTime = ZonedDateTime.of(2024, 11, 1, 11, 0, 0, 0, ZoneOffset.UTC)

      val result = LogFilter.matchesTime(Some(fromTime), Some(toTime), logTime)

      result shouldBe true
    }

    "return true if the log time is equal to from time and to time" in {
      val fromTime = LocalDate.of(2024, 11, 1)
      val toTime = LocalDate.of(2024, 11, 1)
      val logTime = ZonedDateTime.of(2024, 11, 1, 10, 0, 0, 0, ZoneOffset.UTC)

      val result = LogFilter.matchesTime(Some(fromTime), Some(toTime), logTime)

      result shouldBe true
    }

    "return true if no from and to time are provided" in {
      val logTime = ZonedDateTime.of(2024, 11, 1, 11, 0, 0, 0, ZoneOffset.UTC)

      val result = LogFilter.matchesTime(None, None, logTime)

      result shouldBe true
    }

    "return false if the log time is before the from time" in {
      val fromTime = LocalDate.of(2024, 11, 1)
      val toTime = LocalDate.of(2024, 11, 1)
      val logTime = ZonedDateTime.of(2024, 10, 1, 9, 0, 0, 0, ZoneOffset.UTC)

      val result = LogFilter.matchesTime(Some(fromTime), Some(toTime), logTime)

      result shouldBe false
    }

    "return false if the log time is after the to time" in {
      val fromTime = LocalDate.of(2024, 11, 1)
      val toTime = LocalDate.of(2024, 11, 1)
      val logTime = ZonedDateTime.of(2024, 12, 1, 13, 0, 0, 0, ZoneOffset.UTC)

      val result = LogFilter.matchesTime(Some(fromTime), Some(toTime), logTime)

      result shouldBe false
    }

    "return true if the log time is after from time and no to time is provided" in {
      val fromTime = LocalDate.of(2024, 11, 1)
      val logTime = ZonedDateTime.of(2024, 11, 1, 11, 0, 0, 0, ZoneOffset.UTC)

      val result = LogFilter.matchesTime(Some(fromTime), None, logTime)

      result shouldBe true
    }

    "return true if the log time is before to time and no from time is provided" in {
      val toTime = LocalDate.of(2024, 11, 1)
      val logTime = ZonedDateTime.of(2024, 11, 1, 11, 0, 0, 0, ZoneOffset.UTC)

      val result = LogFilter.matchesTime(None, Some(toTime), logTime)

      result shouldBe true
    }
  }

  "LogFilter.matchesField" should {
    val logRecord = LogRecord(
      InetAddress.getByName("127.0.0.1"),
      "testUser",
      ZonedDateTime.now(),
      Method.GET,
      "/home",
      HttpVersion.`HTTP/1.1`,
      Status.Ok,
      1234,
      "http://referer.com",
      "Mozilla/5.0"
    )

    "return true for matching address" in {
      val result =
        LogFilter.matchesField(Some("address"), Some("127.0.0.1"), logRecord)
      result shouldBe true
    }

    "return false for non-matching address" in {
      val result =
        LogFilter.matchesField(Some("address"), Some("192.168.1.1"), logRecord)
      result shouldBe false
    }

    "return true for matching user" in {
      val result =
        LogFilter.matchesField(Some("user"), Some("testUser"), logRecord)
      result shouldBe true
    }

    "return false for non-matching user" in {
      val result =
        LogFilter.matchesField(Some("user"), Some("wrongUser"), logRecord)
      result shouldBe false
    }

    "return true for matching method" in {
      val result =
        LogFilter.matchesField(Some("method"), Some("GET"), logRecord)
      result shouldBe true
    }

    "return false for non-matching method" in {
      val result =
        LogFilter.matchesField(Some("method"), Some("POST"), logRecord)
      result shouldBe false
    }

    "return true for matching resource" in {
      val result =
        LogFilter.matchesField(Some("resource"), Some("/home"), logRecord)
      result shouldBe true
    }

    "return false for non-matching resource" in {
      val result =
        LogFilter.matchesField(Some("resource"), Some("/about"), logRecord)
      result shouldBe false
    }

    "return true for matching protocol" in {
      val result =
        LogFilter.matchesField(Some("protocol"), Some("HTTP/1.1"), logRecord)
      result shouldBe true
    }

    "return false for non-matching protocol" in {
      val result =
        LogFilter.matchesField(Some("protocol"), Some("HTTP/2.0"), logRecord)
      result shouldBe false
    }

    "return true for matching status" in {
      val result =
        LogFilter.matchesField(Some("status"), Some("200"), logRecord)
      result shouldBe true
    }

    "return false for non-matching status" in {
      val result =
        LogFilter.matchesField(Some("status"), Some("404"), logRecord)
      result shouldBe false
    }

    "return true for matching bytesSent" in {
      val result =
        LogFilter.matchesField(Some("bytessent"), Some("1234"), logRecord)
      result shouldBe true
    }

    "return false for non-matching bytesSent" in {
      val result =
        LogFilter.matchesField(Some("bytesSent"), Some("9999"), logRecord)
      result shouldBe false
    }

    "return true for matching referer" in {
      val result = LogFilter.matchesField(
        Some("referer"),
        Some("http://referer.com"),
        logRecord
      )
      result shouldBe true
    }

    "return false for non-matching referer" in {
      val result = LogFilter.matchesField(
        Some("referer"),
        Some("http://wrongreferer.com"),
        logRecord
      )
      result shouldBe false
    }

    "return true for matching userAgent" in {
      val result = LogFilter.matchesField(
        Some("useragent"),
        Some("Mozilla/5.0"),
        logRecord
      )
      result shouldBe true
    }

    "return false for non-matching userAgent" in {
      val result = LogFilter.matchesField(
        Some("userAgent"),
        Some("Mozilla/4.0"),
        logRecord
      )
      result shouldBe false
    }

    "return true if both field and value are None" in {
      val result = LogFilter.matchesField(None, None, logRecord)
      result shouldBe true
    }

    "return false for unknown field" in {
      val result =
        LogFilter.matchesField(Some("unknownField"), Some("value"), logRecord)
      result shouldBe false
    }
  }
}
