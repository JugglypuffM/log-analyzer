package domain.io

import cats.implicits.*
import fs2.io.file.Path

import java.nio.file.{FileSystems, PathMatcher}

case class GlobPattern(pattern: String) {
  val (baseDirectory, pathMatcher): (Path, PathMatcher) =
    val normalizedInput = pattern.strip()
    val separatorIndex = normalizedInput.lastIndexOf("/")

    if (separatorIndex == -1)
      (Path("."), fileSystem.getPathMatcher(s"glob:$normalizedInput"))
    else
      val maybeBaseDir = normalizedInput.substring(0, separatorIndex)
      val maybeMatcher = normalizedInput.substring(separatorIndex + 1)
      val dirSkipIndex = maybeBaseDir.indexOf("**")
      if (dirSkipIndex == -1)
        (Path(maybeBaseDir), fileSystem.getPathMatcher(s"glob:$maybeMatcher"))
      else
        val baseDir = Path(maybeBaseDir.substring(0, dirSkipIndex))
        val skippedDirs = maybeBaseDir.substring(dirSkipIndex)
        val matcher =
          fileSystem.getPathMatcher(s"glob:$skippedDirs$maybeMatcher")

        (baseDir, matcher)
  private val fileSystem = FileSystems.getDefault
}
