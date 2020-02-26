package rssFeednGT

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


object RssReader {
  def main(args : Array[String]):Unit = {
    val system: ActorSystem = ActorSystem("Fetcher")
    implicit val timeout: Timeout = Timeout(30.seconds)
    val SubReader = system.actorOf(Props[SubscriptionReader])

    for {
      urls <- (SubReader ? "rss.json").mapTo[Seq[String]]
      url <- urls
    } system.actorOf(Props[Fetcher]) ! url
    Thread.sleep(5000)
    system.actorOf(Props[Aggregator]) ! timeout
    //system.shutdown()
  }
}
