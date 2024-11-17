package io

import cats.effect.{Async, Resource}
import cats.implicits.*
import fs2.io.file.Flags.Read
import fs2.io.file.{Files, Path}
import fs2.io.readInputStream
import fs2.{Stream, text}

import java.io.InputStream
import java.net.URL

object LogReader {
  def fromUrl[F[_]: Async](url: URL): Stream[F, String] =
    val inputStreamResource: Resource[F, InputStream] =
      Resource.fromAutoCloseable(url.openStream().pure[F])

    Stream
      .resource(inputStreamResource)
      .flatMap(is => readInputStream(is.pure[F], 4096))
      .through(text.utf8.decode)
      .through(text.lines)

  def fromFileStream[F[_]: Async](paths: Stream[F, Path]): Stream[F, String] =
    paths.flatMap(fromFile)

  def fromFile[F[_]: Async](path: Path): Stream[F, String] =
    Files[F]
      .readAll(path, 4096, Read)
      .through(text.utf8.decode)
      .through(text.lines)
}
