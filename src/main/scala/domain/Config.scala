package domain

import domain.Format.Markdown
import fs2.io.file.Path

import java.net.URL
import java.nio.file.{FileSystems, PathMatcher}
import java.time.LocalDate

case class Config(
                   file: Option[(Path, PathMatcher)] = None,
                   url: Option[URL] = None,
                   from: Option[LocalDate] = None,
                   to: Option[LocalDate] = None,
                   format: Format = Markdown,
                   filterField: Option[String] = None,
                   filterValue: Option[String] = None
)
