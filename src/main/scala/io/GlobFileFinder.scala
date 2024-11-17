package io

import cats.implicits.*
import fs2.Stream
import fs2.io.file.{Files, Path}

import java.nio.file.{FileSystems, PathMatcher}

object GlobFileFinder {
  private val fileSystem = FileSystems.getDefault

  def getBaseDirAndPattern(
      path: String
  ): Either[Throwable, (Path, PathMatcher)] =
    val normalizedInput = path.strip()
    val separatorIndex = normalizedInput.lastIndexOf("/")

    Either.catchNonFatal(
      if (separatorIndex == -1)
        (Path("."), fileSystem.getPathMatcher(s"glob:$normalizedInput"))
      else
        val maybeBaseDir = normalizedInput.substring(0, separatorIndex)
        val maybePattern = normalizedInput.substring(separatorIndex + 1)
        val dirSkipIndex = maybeBaseDir.indexOf("**")
        if (dirSkipIndex == -1)
          (Path(maybeBaseDir), fileSystem.getPathMatcher(s"glob:$maybePattern"))
        else
          val baseDir = Path(maybeBaseDir.substring(0, dirSkipIndex))
          val skippedDirs = maybeBaseDir.substring(dirSkipIndex)
          val pattern =
            fileSystem.getPathMatcher(s"glob:$skippedDirs$maybePattern")

          (baseDir, pattern)
    )

  def findFilesByGlob[F[_]: Files](
      baseDir: Path,
      globPattern: PathMatcher
  ): Stream[F, Path] = {
    Files[F]
      .walk(baseDir)
      .filter(path => globPattern.matches(baseDir.relativize(path).toNioPath))
  }
}
