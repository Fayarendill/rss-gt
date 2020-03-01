package rssFeednGT

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import com.typesafe.scalalogging._
import org.slf4j.LoggerFactory

object RssReader {
  private val logger = Logger(LoggerFactory.getLogger(this.getClass))

  def main(args : Array[String]):Unit = {
    val system: ActorSystem = ActorSystem("Fetcher")
    implicit val timeout: Timeout = Timeout(30.seconds)
    val SubReader = system.actorOf(Props[SubscriptionReader])

    (SubReader ? "rss.json").onComplete {
      case Success(urls) => (system.actorOf(Props[Fetcher]) ? urls).onComplete {
        case Success(_) => (system.actorOf(Props[Fetcher]) ? ()).onComplete {
          case Success(headlines) => system.actorOf(Props[Dumper]) ! headlines
          case Failure(exception) => logger.error(s"failed to get headlines cache ex = $exception")
        }
        case Failure(exception) => logger.error(s"failed to get headlines ex = $exception")
      }
      case Failure(exception) => logger.error(s"failed to get urls ex = $exception")
    }

  }
  
}
