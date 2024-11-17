package io

import cats.implicits.*
import domain.io.GlobPattern
import fs2.Stream
import fs2.io.file.{Files, Path}

import java.nio.file.{FileSystems, PathMatcher}

object GlobFileFinder {
  def fromGlobs[F[_]: Files](globs: List[GlobPattern]): Stream[F, Path] =
    Stream.emits(globs).flatMap(fromGlob)

  def fromGlob[F[_]: Files](glob: GlobPattern): Stream[F, Path] = {
    Files[F]
      .walk(glob.baseDirectory)
      .filter(path =>
        glob.pathMatcher.matches(glob.baseDirectory.relativize(path).toNioPath)
      )
  }
}
