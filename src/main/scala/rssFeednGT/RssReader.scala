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
  private val logger = Logger(LoggerFactory.getLogger(this.getClass))
  val SubReader = system.actorOf(Props[SubscriptionReader])
  implicit val timeout: Timeout = Timeout(5.seconds)

  val port = 8080

  (SubReader ? "rss.json").onComplete {
    case Success(urls) => (system.actorOf(Props[Fetcher]) ? urls)
    case Failure(exception) => logger.error(s"failed to get urls ex = $exception")
  }

  val bindingFuture =
    Http().bindAndHandle(HeadlinesRoute.headlinesRoute(system), "localhost", port)

  logger.info(s"Server started at the port $port")
}


