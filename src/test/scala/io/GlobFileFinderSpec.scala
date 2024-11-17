package io

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import domain.io.GlobPattern
import fs2.io.file.{Files, Path}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

class GlobFileFinderSpec extends AsyncWordSpec with AsyncIOSpec with Matchers {
  "findFilesByGlob" should {
    val resourcesPath = "src/test/resources"

    "find files that match the glob pattern" in {
      GlobFileFinder
        .fromGlob[IO](GlobPattern(resourcesPath + "/*.txt"))
        .compile
        .toList
        .map { result =>
          result should contain allElementsOf List(
            Path(resourcesPath + "/logs.txt"),
            Path(resourcesPath + "/logs_4.txt")
          )
        }
    }

    "return an empty list when no files match the glob pattern" in {
      GlobFileFinder
        .fromGlob[IO](GlobPattern(resourcesPath + "**/*.csv"))
        .compile
        .toList
        .map { result =>
          result shouldBe empty
        }
    }

    "find files in subdirectories matching the glob pattern" in {
      GlobFileFinder
        .fromGlob[IO](GlobPattern(resourcesPath + "**/*.txt"))
        .compile
        .toList
        .map { result =>
          result should contain allElementsOf List(
            Path(resourcesPath + "/dir1/logs_1.txt"),
            Path(resourcesPath + "/dir2/logs_2.txt"),
            Path(resourcesPath + "/dir1/dir3/logs_3.txt"),
            Path(resourcesPath + "/dir1/logs_5.txt")
          )
        }
    }
  }
}
