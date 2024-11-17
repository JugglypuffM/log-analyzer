package domain.io

enum Format {
  case Markdown, AsciiDoc
}

object Format {
  def parseFormat(input: String): Option[Format] =
    input.strip().toLowerCase() match
      case "md" | "markdown"   => Some(Markdown)
      case "adoc" | "asciidoc" => Some(AsciiDoc)
      case _                   => None
}
