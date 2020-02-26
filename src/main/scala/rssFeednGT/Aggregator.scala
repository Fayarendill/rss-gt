package rssFeednGT

import akka.{Done, NotUsed}
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class Aggregator extends Actor with ActorLogging {
  implicit val materializer = ActorMaterializer()

//  var source: Source[Headline, NotUsed] = Source.empty[Headline]

//  val distinctFlow = Flow[Headline]
  val consoleSink: Sink[Headline, Future[Done]] = Sink.foreach[Headline](println)

  def consoleDump(timeout: Timeout): Unit = {
    implicit val tout = timeout
    val fetcher = context.actorOf(Props[Fetcher])
    (fetcher ? ()).mapTo[Source[Headline, NotUsed]].onComplete {
      case Success(src) => {
        log.info("headlines arrived")
        src.runWith(consoleSink)
      }
      case Failure(exception) => log.error(s"failed to get headlines ex=$exception")
    }
  }

  def receive(): Receive = {
    case (timeout: Timeout) => consoleDump(timeout)
  }
}
