package domain

import org.http4s.Status

case class Statistics(
    numberOfRequests: Long,
    resourcesFrequency: Map[String, Long],
    codesFrequency: Map[Status, Int],
    responseByteSizes: List[Int]
)
