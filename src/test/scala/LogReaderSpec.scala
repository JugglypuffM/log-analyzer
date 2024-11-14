import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

class LogReaderSpec extends AsyncWordSpec with AsyncIOSpec with Matchers {
  "LogReader" should {
    "read from file" in {
      val testDataPath = getClass.getResource("logs.txt").getPath

      val data = LogReader.fromFile[IO](
        testDataPath
      )

      val expectedData: List[String] = List(
        "216.46.173.126 - - [03/Jun/2015:17:06:00 +0000] \"GET /downloads/product_1 HTTP/1.1\" 404 328 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.16)\"",
        "180.179.174.219 - - [03/Jun/2015:17:06:01 +0000] \"GET /downloads/product_2 HTTP/1.1\" 200 490 \"-\" \"Debian APT-HTTP/1.3 (0.9.7.9)\"",
        "invalid line",
        "180.179.174.219 - - [03/Jun/2015:17:06:17 +0000] \"GET /downloads/product_2 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3 (0.9.7.9)\"",
        "180.179.174.219 - - [03/Jun/2015:17:06:34 +0000] \"GET /downloads/product_2 HTTP/1.1\" 404 346 \"-\" \"Debian APT-HTTP/1.3 (0.9.7.9)\""
      )

      data.take(5).compile.toList.asserting(_ shouldBe expectedData)
    }

    "read from url" in {
      val testDataUrl =
        "https://raw.githubusercontent.com/elastic/examples/master/Common%20Data%20Formats/nginx_logs/nginx_logs"

      val data = LogReader.fromUrl[IO](testDataUrl)

      val expectedData: List[String] = List(
        "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"",
        "93.180.71.3 - - [17/May/2015:08:05:23 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"",
        "80.91.33.133 - - [17/May/2015:08:05:24 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.17)\"",
        "217.168.17.5 - - [17/May/2015:08:05:34 +0000] \"GET /downloads/product_1 HTTP/1.1\" 200 490 \"-\" \"Debian APT-HTTP/1.3 (0.8.10.3)\"",
        "217.168.17.5 - - [17/May/2015:08:05:09 +0000] \"GET /downloads/product_2 HTTP/1.1\" 200 490 \"-\" \"Debian APT-HTTP/1.3 (0.8.10.3)\""
      )

      data.take(5).compile.toList.asserting(_ shouldBe expectedData)
    }
  }
}
