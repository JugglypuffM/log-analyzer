package io

import cats.Applicative
import cats.implicits.*
import domain.io.{Config, Format, GlobPattern}
import fs2.io.file.Path
import scopt.OParser

import java.net.URI
import java.nio.file.FileSystems
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Try

object ConfigReader {
  private val builder = OParser.builder[Config]
  private val parser: OParser[Unit, Config] = {
    import builder.*

    val dateTimeFormatter =
      DateTimeFormatter.ISO_DATE

    val methodList =
      List(
        "address",
        "user",
        "method",
        "resource",
        "protocol",
        "status",
        "bytesSent",
        "referer",
        "useragent"
      )

    def validateFilePath(input: String): Either[String, Unit] =
      Try(GlobPattern(input))
        .map(_ => success)
        .getOrElse(failure(s"Failed to parse path $input"))

    def validateUrl(input: String): Either[String, Unit] =
      Try(URI(input).toURL)
        .map(_ => success)
        .getOrElse(failure(s"Invalid URL $input"))

    def validateTime(input: String): Either[String, Unit] =
      Try(LocalDate.parse(input, dateTimeFormatter))
        .map(_ => success)
        .getOrElse(failure(s"Invalid time format"))

    def validateFormat(input: String): Either[String, Unit] =
      Try(Format.parseFormat(input))
        .map(_ => success)
        .getOrElse(failure(s"No available format with name $input"))

    def validateField(input: String): Either[String, Unit] =
      if (methodList.contains(input.strip().toLowerCase())) success
      else
        failure(
          "No such field in record, or time was specified, which is handled by options --from and --to"
        )

    OParser.sequence(
      programName("analyzer"),
      opt[String]("file")
        .unbounded()
        .validate(validateFilePath)
        .action((x, c) => c.copy(files = GlobPattern(x) :: c.files))
        .text("Path to log file, may be a glob-pattern"),
      opt[String]("url")
        .unbounded()
        .validate(validateUrl)
        .action((x, c) => c.copy(urls = URI(x).toURL :: c.urls))
        .text("Get logs from Url"),
      opt[String]("from")
        .validate(validateTime)
        .action((x, c) =>
          c.copy(from = Some(LocalDate.parse(x, dateTimeFormatter)))
        )
        .text("Analyze logs that come after this date"),
      opt[String]("to")
        .validate(validateTime)
        .action((x, c) =>
          c.copy(to = Some(LocalDate.parse(x, dateTimeFormatter)))
        )
        .text("Analyze logs that come before this date"),
      opt[String]("format")
        .validate(validateFormat)
        .action((x, c) =>
          c.copy(format = Format.parseFormat(x).getOrElse(c.format))
        )
        .text("Output format, may be Markdown(md) or AsciiDoc(adoc)"),
      opt[String]("field")
        .validate(validateField)
        .action((x, c) => c.copy(filterField = Some(x.toLowerCase)))
        .text(s"Filter logs by fields: ${methodList.mkString(", ")}"),
      opt[String]("value")
        .action((x, c) => c.copy(filterValue = Some(x)))
        .text("Value of filter field"),
      checkConfig { c =>
        (c.filterField, c.filterValue) match {
          case (Some(_), Some(_)) => success
          case (None, None)       => success
          case _ =>
            failure("Both --field and --value must be specified together")
        }
      },
      checkConfig { c =>
        (c.files, c.urls) match
          case (Nil, Nil) => failure("No log sources provided")
          case _          => success
      }
    )
  }

  def readConfig[F[_]: Applicative](args: List[String]): F[Option[Config]] =
    OParser.parse(parser, args, Config()).pure[F]
}
