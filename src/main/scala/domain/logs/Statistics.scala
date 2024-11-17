package domain.logs

import domain.io.Format
import fs2.io.file.Path
import org.http4s.Status

import java.net.{InetAddress, URL}
import java.time.ZonedDateTime
import scala.collection.immutable.ListMap

case class Statistics(
    numberOfRequests: Long,
    resourcesFrequency: Map[String, Int],
    codesFrequency: Map[Status, Int],
    addressFrequency: Map[InetAddress, Int],
    userAgentsFrequency: Map[String, Int],
    responseByteSizes: List[Int],
    startDate: Option[ZonedDateTime],
    endDate: Option[ZonedDateTime]
) {
  private def percentile(values: List[Int], p: Double): Int = {
    val sorted = values.sorted
    val index = (p / 100.0 * (sorted.size - 1)).round.toInt
    sorted.lift(index).getOrElse(0)
  }

  private def sortDescending[R](map: Map[R, Int]): ListMap[R, Int] = ListMap(
    map.toSeq.sortBy(-_._2): _*
  )

  def generateReport(
      files: List[Path],
      urls: List[URL],
      format: Format
  ): String = {
    val totalRequests = numberOfRequests
    val averageResponseSize =
      if (responseByteSizes.nonEmpty)
        responseByteSizes.map(_.toLong).sum / numberOfRequests
      else 0
    val p95ResponseSize = percentile(responseByteSizes, 95)
    val mostPopularResources = sortDescending(resourcesFrequency).take(5)
    val responseCodes = sortDescending(codesFrequency).take(5)
    val frequentAddresses = sortDescending(addressFrequency).take(5)
    val frequentUserAgents = sortDescending(userAgentsFrequency).take(5)

    format match {
      case Format.Markdown =>
        s"""
            |#### Источники логов
            || Тип     | Значение             |
            ||:--------:|:-------------------|
            |${(files.map(file => s"| Файл | $file |") :::
            urls.map(url => s"| URL | $url |")).mkString("\n|")}
            |
            |#### Общая информация
            |
            || Метрика              | Значение       |
            ||:---------------------:|-------------:|
            || Начальная дата       | ${startDate
            .map(_.toString)
            .getOrElse("-")} |
            || Конечная дата        | ${endDate
            .map(_.toString)
            .getOrElse("-")} |
            || Количество запросов  | $totalRequests |
            || Средний размер ответа | ${averageResponseSize}b |
            || 95p размера ответа   | ${p95ResponseSize}b |
            |
            |#### Запрашиваемые ресурсы
            |
            || Ресурс         | Количество   |
            ||:---------------:|-----------:|
            |${mostPopularResources
            .map { case (resource, count) => s"| $resource | $count |" }
            .mkString("\n|")}
            |
            |#### Коды ответа
            |
            || Код | Имя           | Количество |
            ||:---:|:--------------:|---------:|
            |${responseCodes
            .map { case (status, count) =>
              s"| ${status.code} | ${status.reason} | $count |"
            }
            .mkString("\n|")}
            |
            |#### Частые адреса
            |
            || Адрес          | Количество   |
            ||:---------------:|-----------:|
            |${frequentAddresses
            .map { case (address, count) => s"| $address | $count |" }
            .mkString("\n|")}
            |
            |#### Частые агенты
            |
            || Агент          | Количество   |
            ||:---------------:|-----------:|
            |${frequentUserAgents
            .map { case (agent, count) => s"| $agent | $count |" }
            .mkString("\n|")}
            |
            |""".stripMargin

      case Format.AsciiDoc =>
        s"""
           |=== Источники логов
           |[cols="2,2"]
           ||===
           || Тип     | Значение
           |${(files.map(file => s"| Файл | $file") ::: urls.map(url =>
            s"| URL | $url"
          )).mkString("\n|")}
           ||===
           |
           |=== Общая информация
           |
           |[cols="2,2"]
           ||===
           || Метрика              | Значение
           || Начальная дата       | ${startDate
            .map(_.toString)
            .getOrElse("-")}
           || Конечная дата        | ${endDate.map(_.toString).getOrElse("-")}
           || Количество запросов  | $totalRequests
           || Средний размер ответа | ${averageResponseSize}b
           || 95p размера ответа   | ${p95ResponseSize}b
           ||===
           |
           |=== Запрашиваемые ресурсы
           |
           |[cols="2,2"]
           ||===
           || Ресурс         | Количество
           |${mostPopularResources
            .map { case (resource, count) => s"| $resource | $count" }
            .mkString("\n|")}
           ||===
           |
           |=== Коды ответа
           |
           |[cols="3,2,2"]
           ||===
           || Код | Имя          | Количество
           |${responseCodes
            .map { case (status, count) =>
              s"| ${status.code} | ${status.reason} | $count"
            }
            .mkString("\n|")}
           ||===
           |
           |=== Частые адреса
           |
           |[cols="2,2"]
           ||===
           || Адрес          | Количество
           |${frequentAddresses
            .map { case (address, count) => s"| $address | $count" }
            .mkString("\n|")}
           ||===
           |
           |=== Частые агенты
           |
           |[cols="2,2"]
           ||===
           || Агент          | Количество
           |${frequentUserAgents
            .map { case (agent, count) => s"| $agent | $count" }
            .mkString("\n|")}
           ||===
           |""".stripMargin
    }
  }
}
