package domain.io

import fs2.io.file.Path
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.util.regex.PatternSyntaxException

class GlobPatternSpec extends AnyWordSpec with Matchers {
  "GlobFileFinder.getBaseDirAndPattern" should {

    "return the current directory and a PathMatcher for a simple glob pattern without separator" in {
      val result = GlobPattern("*.txt")
      val (baseDir, pattern) = (result.baseDirectory, result.pathMatcher)

      assert(baseDir == Path("."))
      assert(pattern.matches(Path("example.txt").toNioPath))
      assert(!pattern.matches(Path("example.csv").toNioPath))
    }

    "return the correct base directory and PathMatcher for a valid path with separator" in {
      val result = GlobPattern("src/main/*.scala")
      val (baseDir, pattern) = (result.baseDirectory, result.pathMatcher)

      assert(baseDir == Path("src/main"))
      assert(pattern.matches(Path("File.scala").toNioPath))
      assert(!pattern.matches(Path("File.java").toNioPath))
    }

    "handle an invalid glob pattern gracefully and return a Left with Throwable" in {
      val result = intercept[PatternSyntaxException](GlobPattern("*.{txt,"))
      result.getMessage shouldBe "Missing '} near index 6\n*.{txt,\n      ^"
    }

    "return the current directory for an empty string" in {
      val result = GlobPattern("")
      val (baseDir, pattern) = (result.baseDirectory, result.pathMatcher)

      assert(baseDir == Path("."))
      assert(pattern.matches(Path("").toNioPath))
    }

    "return a base directory and a PathMatcher for a single file path" in {
      val result = GlobPattern("folder/file.txt")
      val (baseDir, pattern) = (result.baseDirectory, result.pathMatcher)

      assert(baseDir == Path("folder"))
      assert(pattern.matches(Path("file.txt").toNioPath))
      assert(!pattern.matches(Path("file.csv").toNioPath))
    }

    "return a base directory and a PathMatcher for skipped directories" in {
      val result = GlobPattern("folder/**/*.txt")
      val (baseDir, pattern) = (result.baseDirectory, result.pathMatcher)

      assert(baseDir == Path("folder"))
      assert(pattern.matches(Path("file.txt").toNioPath))
      assert(pattern.matches(Path("my/file.txt").toNioPath))
      assert(!pattern.matches(Path("file.csv").toNioPath))
      assert(!pattern.matches(Path("my/file.csv").toNioPath))
    }

    "return a base directory and a PathMatcher for skipped directories when no wile pattern provided" in {
      val result = GlobPattern("folder/**/dir")
      val (baseDir, pattern) = (result.baseDirectory, result.pathMatcher)

      assert(baseDir == Path("folder"))
      assert(pattern.matches(Path("a/dir/").toNioPath))
      assert(!pattern.matches(Path("a/dir/file.txt").toNioPath))
      assert(!pattern.matches(Path("file.txt").toNioPath))
      assert(!pattern.matches(Path("my/file.txt").toNioPath))
      assert(!pattern.matches(Path("file.csv").toNioPath))
      assert(!pattern.matches(Path("my/file.csv").toNioPath))
    }

    "return a base directory and a PathMatcher for absolute path" in {
      val result = GlobPattern(
        "/home/jugglylinux/things/IntelliJ/3-scala-JugglypuffM/logs.txt"
      )
      val (baseDir, pattern) = (result.baseDirectory, result.pathMatcher)

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
}
