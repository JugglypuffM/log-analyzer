package domain.logs

import domain.io.Format
import fs2.io.file.Path
import org.http4s.Status
import org.scalatest.wordspec.AnyWordSpec

import java.net.{InetAddress, URI}
import java.time.ZonedDateTime

class StatisticSpec extends AnyWordSpec {
  "generateReport" should {

    "generate a Markdown report correctly" in {
      val stats = Statistics(
        numberOfRequests = 3,
        resourcesFrequency = Map(
          "/index.html" -> 1,
          "/about.html" -> 1,
          "/contact.html" -> 1
        ),
        codesFrequency = Map(
          Status.Ok -> 1,
          Status.NotFound -> 1,
          Status.InternalServerError -> 1
        ),
        addressFrequency = Map(
          InetAddress.getByName("192.168.1.1") -> 2,
          InetAddress.getByName("192.168.1.2") -> 1
        ),
        userAgentsFrequency = Map("Mozilla/5.0" -> 2, "curl/7.68.0" -> 1),
        responseByteSizes = List(500, 800, 950),
        startDate = Some(ZonedDateTime.parse("2024-11-01T00:00:00Z")),
        endDate = Some(ZonedDateTime.parse("2024-11-10T23:59:59Z"))
      )

      val expectedMarkdown =
        """
          |#### Источники логов
          || Тип     | Значение             |
          ||:--------:|:-------------------|
          || Файл | logs.txt |
          || URL | http://example.com/logs |
          |
          |#### Общая информация
          |
          || Метрика              | Значение       |
          ||:---------------------:|-------------:|
          || Начальная дата       | 2024-11-01T00:00Z |
          || Конечная дата        | 2024-11-10T23:59:59Z |
          || Количество запросов  | 3 |
          || Средний размер ответа | 750b |
          || 95p размера ответа   | 950b |
          |
          |#### Запрашиваемые ресурсы
          |
          || Ресурс         | Количество   |
          ||:---------------:|-----------:|
          || /index.html | 1 |
          || /about.html | 1 |
          || /contact.html | 1 |
          |
          |#### Коды ответа
          |
          || Код | Имя           | Количество |
          ||:---:|:--------------:|---------:|
          || 200 | OK | 1 |
          || 404 | Not Found | 1 |
          || 500 | Internal Server Error | 1 |
          |
          |#### Частые адреса
          |
          || Адрес          | Количество   |
          ||:---------------:|-----------:|
          || /192.168.1.1 | 2 |
          || /192.168.1.2 | 1 |
          |
          |#### Частые агенты
          |
          || Агент          | Количество   |
          ||:---------------:|-----------:|
          || Mozilla/5.0 | 2 |
          || curl/7.68.0 | 1 |
          |
          |""".stripMargin

      val actualMarkdown = stats.generateReport(
        List(Path("logs.txt")),
        List(URI("http://example.com/logs").toURL),
        Format.Markdown
      )

      assert(actualMarkdown == expectedMarkdown)
    }

    "generate an AsciiDoc report correctly" in {
      val stats = Statistics(
        numberOfRequests = 3,
        resourcesFrequency = Map(
          "/index.html" -> 1,
          "/about.html" -> 1,
          "/contact.html" -> 1
        ),
        codesFrequency = Map(
          Status.Ok -> 1,
          Status.NotFound -> 1,
          Status.InternalServerError -> 1
        ),
        addressFrequency = Map(
          InetAddress.getByName("192.168.1.1") -> 2,
          InetAddress.getByName("192.168.1.2") -> 1
        ),
        userAgentsFrequency = Map("Mozilla/5.0" -> 2, "curl/7.68.0" -> 1),
        responseByteSizes = List(500, 800, 950),
        startDate = Some(ZonedDateTime.parse("2024-11-01T00:00:00Z")),
        endDate = Some(ZonedDateTime.parse("2024-11-10T23:59:59Z"))
      )

      val expectedAsciiDoc =
        """
          |=== Источники логов
          |[cols="2,2"]
          ||===
          || Тип     | Значение
          || Файл | logs.txt
          || URL | http://example.com/logs
          ||===
          |
          |=== Общая информация
          |
          |[cols="2,2"]
          ||===
          || Метрика              | Значение
          || Начальная дата       | 2024-11-01T00:00Z
          || Конечная дата        | 2024-11-10T23:59:59Z
          || Количество запросов  | 3
          || Средний размер ответа | 750b
          || 95p размера ответа   | 950b
          ||===
          |
          |=== Запрашиваемые ресурсы
          |
          |[cols="2,2"]
          ||===
          || Ресурс         | Количество
          || /index.html | 1
          || /about.html | 1
          || /contact.html | 1
          ||===
          |
          |=== Коды ответа
          |
          |[cols="3,2,2"]
          ||===
          || Код | Имя          | Количество
          || 200 | OK | 1
          || 404 | Not Found | 1
          || 500 | Internal Server Error | 1
          ||===
          |
          |=== Частые адреса
          |
          |[cols="2,2"]
          ||===
          || Адрес          | Количество
          || /192.168.1.1 | 2
          || /192.168.1.2 | 1
          ||===
          |
          |=== Частые агенты
          |
          |[cols="2,2"]
          ||===
          || Агент          | Количество
          || Mozilla/5.0 | 2
          || curl/7.68.0 | 1
          ||===
          |""".stripMargin

      val actualAsciiDoc = stats.generateReport(
        List(Path("logs.txt")),
        List(URI("http://example.com/logs").toURL),
        Format.AsciiDoc
      )

      assert(actualAsciiDoc == expectedAsciiDoc)
    }

    "generate a correct Markdown report for empty statistics" in {
      val emptyStats = Statistics(
        numberOfRequests = 0,
        resourcesFrequency = Map.empty,
        codesFrequency = Map.empty,
        addressFrequency = Map.empty,
        userAgentsFrequency = Map.empty,
        responseByteSizes = List.empty,
        startDate = None,
        endDate = None
      )

      val expectedMarkdown =
        """
          |#### Источники логов
          || Тип     | Значение             |
          ||:--------:|:-------------------|
          || Файл | logs.txt |
          || URL | http://example.com/logs |
          |
          |#### Общая информация
          |
          || Метрика              | Значение       |
          ||:---------------------:|-------------:|
          || Начальная дата       | - |
          || Конечная дата        | - |
          || Количество запросов  | 0 |
          || Средний размер ответа | 0b |
          || 95p размера ответа   | 0b |
          |
          |#### Запрашиваемые ресурсы
          |
          || Ресурс         | Количество   |
          ||:---------------:|-----------:|
          |
          |
          |#### Коды ответа
          |
          || Код | Имя           | Количество |
          ||:---:|:--------------:|---------:|
          |
          |
          |#### Частые адреса
          |
          || Адрес          | Количество   |
          ||:---------------:|-----------:|
          |
          |
          |#### Частые агенты
          |
          || Агент          | Количество   |
          ||:---------------:|-----------:|
          |
          |
          |""".stripMargin

      val actualMarkdown = emptyStats.generateReport(
        List(Path("logs.txt")),
        List(URI("http://example.com/logs").toURL),
        Format.Markdown
      )

      assert(actualMarkdown == expectedMarkdown)
    }

    "generate a correct AsciiDoc report for empty statistics" in {
      val emptyStats = Statistics(
        numberOfRequests = 0,
        resourcesFrequency = Map.empty,
        codesFrequency = Map.empty,
        addressFrequency = Map.empty,
        userAgentsFrequency = Map.empty,
        responseByteSizes = List.empty,
        startDate = None,
        endDate = None
      )

      val expectedAsciiDoc =
        """
          |=== Источники логов
          |[cols="2,2"]
          ||===
          || Тип     | Значение
          || Файл | logs.txt
          || URL | http://example.com/logs
          ||===
          |
          |=== Общая информация
          |
          |[cols="2,2"]
          ||===
          || Метрика              | Значение
          || Начальная дата       | -
          || Конечная дата        | -
          || Количество запросов  | 0
          || Средний размер ответа | 0b
          || 95p размера ответа   | 0b
          ||===
          |
          |=== Запрашиваемые ресурсы
          |
          |[cols="2,2"]
          ||===
          || Ресурс         | Количество
          |
          ||===
          |
          |=== Коды ответа
          |
          |[cols="3,2,2"]
          ||===
          || Код | Имя          | Количество
          |
          ||===
          |
          |=== Частые адреса
          |
          |[cols="2,2"]
          ||===
          || Адрес          | Количество
          |
          ||===
          |
          |=== Частые агенты
          |
          |[cols="2,2"]
          ||===
          || Агент          | Количество
          |
          ||===
          |""".stripMargin

      val actualAsciiDoc = emptyStats.generateReport(
        List(Path("logs.txt")),
        List(URI("http://example.com/logs").toURL),
        Format.AsciiDoc
      )

      assert(actualAsciiDoc == expectedAsciiDoc)
    }
  }
}
