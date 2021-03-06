package rssFeednGT

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging._
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}


object RssReader extends App {
  implicit val system: ActorSystem = ActorSystem("rss-reader")
  implicit val timeout: Timeout    = Timeout(5.seconds)

  private val logger = Logger(LoggerFactory.getLogger(this.getClass))
  val SubReader      = system.actorOf(Props[SubscriptionReader])
  val fetcher        = system.actorOf(Props[Fetcher])
  val port           = 8080

  (SubReader ? SubscriptionReaderLoad("rss.json")).mapTo[Seq[String]].onComplete {
    case Success(urls) => urls.map { url => fetcher ! FetcherAddUrl(url)}
    case Failure(exception) => logger.error(s"failed to get urls ex = $exception")
  }

  system.scheduler.scheduleWithFixedDelay(1.seconds, 10.seconds, fetcher, FetcherReload())

  val bindingFuture =
    Http().bindAndHandle(HeadlinesRoute.headlinesRoute(system), "localhost", port)

  logger.info(s"Server started at the port $port")
}