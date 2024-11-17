import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.*
import io.{ConfigReader, GlobFileFinder, LogReader}
import logs.analyze.LogAnalyzer
import logs.processing.{LogFilter, LogParser}

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    for {
      configOption <- ConfigReader.readConfig[IO](args)
      config <- IO.fromOption(configOption)(
        IllegalArgumentException("Argument parsing failed")
      )
      paths <- GlobFileFinder.fromGlobs[IO](config.files).compile.toList

      dataFromFiles = LogReader.fromFilesList[IO](paths)
      dataFromUrls = LogReader.fromUrlsList[IO](config.urls)

      dataStream = dataFromFiles ++ dataFromUrls

      logStream = dataStream
        .evalMap(LogParser.parse[IO])
        .collect { case Right(log) => log }
        .through(LogFilter.filterWithConfig(config))
      stats <- LogAnalyzer.collectStatistics(logStream)
      _ <- IO.println(stats.generateReport(paths, config.urls, config.format))
    } yield ExitCode.Success

}
