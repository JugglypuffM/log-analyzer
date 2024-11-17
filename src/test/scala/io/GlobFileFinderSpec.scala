package io

import cats.effect.IO
import cats.effect.kernel.Resource.Pure
import cats.effect.testing.scalatest.AsyncIOSpec
import fs2.io.file.{Files, Path}
import org.scalatest.EitherValues.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.{AnyWordSpec, AsyncWordSpec}

import java.nio.file.FileSystems

class GlobFileFinderSpec extends AsyncWordSpec with AsyncIOSpec with Matchers {

  "GlobFileFinder.getBaseDirAndPattern" should {

    "return the current directory and a PathMatcher for a simple glob pattern without separator" in {
      val result = GlobFileFinder.getBaseDirAndPattern("*.txt")
      assert(result.isRight)
      val (baseDir, pattern) = result.value

      assert(baseDir == Path("."))
      assert(pattern.matches(Path("example.txt").toNioPath))
      assert(!pattern.matches(Path("example.csv").toNioPath))
    }

    "return the correct base directory and PathMatcher for a valid path with separator" in {
      val result = GlobFileFinder.getBaseDirAndPattern("src/main/*.scala")
      assert(result.isRight)
      val (baseDir, pattern) = result.value

      assert(baseDir == Path("src/main"))
      assert(pattern.matches(Path("File.scala").toNioPath))
      assert(!pattern.matches(Path("File.java").toNioPath))
    }

    "handle an invalid glob pattern gracefully and return a Left with Throwable" in {
      val result = GlobFileFinder.getBaseDirAndPattern("*.{txt,")
      assert(result.isLeft)
      result shouldBe a[Left[Throwable, _]]
    }

    "return the current directory for an empty string" in {
      val result = GlobFileFinder.getBaseDirAndPattern("")
      assert(result.isRight)
      val (baseDir, pattern) = result.value

      assert(baseDir == Path("."))
      assert(pattern.matches(Path("").toNioPath))
    }

    "return a base directory and a PathMatcher for a single file path" in {
      val result = GlobFileFinder.getBaseDirAndPattern("folder/file.txt")
      assert(result.isRight)
      val (baseDir, pattern) = result.value

      assert(baseDir == Path("folder"))
      assert(pattern.matches(Path("file.txt").toNioPath))
      assert(!pattern.matches(Path("file.csv").toNioPath))
    }

    "return a base directory and a PathMatcher for skipped directories" in {
      val result = GlobFileFinder.getBaseDirAndPattern("folder/**/*.txt")
      assert(result.isRight)
      val (baseDir, pattern) = result.value

      assert(baseDir == Path("folder"))
      assert(pattern.matches(Path("file.txt").toNioPath))
      assert(pattern.matches(Path("my/file.txt").toNioPath))
      assert(!pattern.matches(Path("file.csv").toNioPath))
      assert(!pattern.matches(Path("my/file.csv").toNioPath))
    }

    "return a base directory and a PathMatcher for skipped directories when no wile pattern provided" in {
      val result = GlobFileFinder.getBaseDirAndPattern("folder/**/dir")
      assert(result.isRight)
      val (baseDir, pattern) = result.value

      assert(baseDir == Path("folder"))
      assert(pattern.matches(Path("a/dir/").toNioPath))
      assert(!pattern.matches(Path("a/dir/file.txt").toNioPath))
      assert(!pattern.matches(Path("file.txt").toNioPath))
      assert(!pattern.matches(Path("my/file.txt").toNioPath))
      assert(!pattern.matches(Path("file.csv").toNioPath))
      assert(!pattern.matches(Path("my/file.csv").toNioPath))
    }

    "return a base directory and a PathMatcher for absolute path" in {
      val result = GlobFileFinder.getBaseDirAndPattern(
        "/home/jugglylinux/things/IntelliJ/3-scala-JugglypuffM/logs.txt"
      )
      assert(result.isRight)
      val (baseDir, pattern) = result.value

      assert(
        baseDir == Path(
          "/home/jugglylinux/things/IntelliJ/3-scala-JugglypuffM/"
        )
      )
      assert(pattern.matches(Path("logs.txt").toNioPath))
      assert(!pattern.matches(Path("file.txt").toNioPath))
      assert(!pattern.matches(Path("my/file.txt").toNioPath))
      assert(!pattern.matches(Path("file.csv").toNioPath))
      assert(!pattern.matches(Path("my/file.csv").toNioPath))
    }

  }

  "findFilesByGlob" should {
    val resourcesPath = "src/test/resources"

    "find files that match the glob pattern" in {
      val baseDir = Path(resourcesPath)

      val globPattern = FileSystems.getDefault.getPathMatcher("glob:*.txt")
      GlobFileFinder
        .findFilesByGlob[IO](baseDir, globPattern)
        .compile
        .toList
        .map { result =>
          result should contain allElementsOf List(
            baseDir.resolve("logs.txt"),
            baseDir.resolve("logs_4.txt")
          )
        }
    }

    "return an empty list when no files match the glob pattern" in {
      val baseDir = Path(resourcesPath)

      val globPattern = FileSystems.getDefault.getPathMatcher("glob:**/*.csv")

      GlobFileFinder
        .findFilesByGlob[IO](baseDir, globPattern)
        .compile
        .toList
        .map { result =>
          result shouldBe empty
        }
    }

    "find files in subdirectories matching the glob pattern" in {
      val baseDir = Path(resourcesPath)

      val globPattern = FileSystems.getDefault.getPathMatcher("glob:**/*.txt")

      GlobFileFinder
        .findFilesByGlob[IO](baseDir, globPattern)
        .compile
        .toList
        .map { result =>
          result should contain allElementsOf List(
            baseDir.resolve("dir1/logs_1.txt"),
            baseDir.resolve("dir2/logs_2.txt"),
            baseDir.resolve("dir1/dir3/logs_3.txt"),
            baseDir.resolve("dir1/logs_5.txt")
          )
        }
    }
  }
}
