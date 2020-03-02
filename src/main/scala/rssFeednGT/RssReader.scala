package rssFeednGT

//import akka.actor.{ActorSystem, Props}
//import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
//import akka.util.Timeout
//
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import akka.actor
import akka.actor.Props
//import akka.actor.typed.{ActorRef, Behavior}
import akka.stream.ActorMaterializer

import scala.util.{Failure, Success}
import com.typesafe.scalalogging._
import org.slf4j.LoggerFactory

//import akka.actor.typed.ActorSystem
//import akka.actor.typed.scaladsl.adapter._
//import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer


object RssReader extends App {
  implicit val system: ActorSystem = ActorSystem("rss-reader")
//  implicit val materializer: ActorMaterializer = ActorMaterializer()
  private val logger = Logger(LoggerFactory.getLogger(this.getClass))
  val SubReader = system.actorOf(Props[SubscriptionReader])
  implicit val timeout: Timeout = Timeout(30.seconds)
//  implicit val log = Logging(system, "main")

  val port = 8080

  (SubReader ? "rss.json").onComplete {
    case Success(urls) => (system.actorOf(Props[Fetcher]) ? urls)
    case Failure(exception) => logger.error(s"failed to get urls ex = $exception")
  }

  val bindingFuture =
    Http().bindAndHandle(HeadlinesRoute.headlinesRoute(system), "localhost", port)

  logger.info(s"Server started at the port $port")
}


