package domain

import org.http4s.Status

import java.net.InetAddress

case class Statistics(
    numberOfRequests: Long,
    resourcesFrequency: Map[String, Long],
    codesFrequency: Map[Status, Int],
    addressFrequency: Map[InetAddress, Int],
    userAgentsFrequency: Map[String, Int],
    responseByteSizes: List[Int]
){

}
