package rssFeednGT

import akka.Done
import akka.actor.{Actor, ActorLogging}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

import scala.concurrent.Future

class Dumper extends Actor with ActorLogging {
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val consoleSink: Sink[Headline, Future[Done]] = Sink.foreach[Headline](println)

  def consoleDump(headlines:Seq[Headline]): Unit = {
    val source = Source.fromIterator(() => headlines.iterator)
//    val size = headlines.size
//    log.debug(s"dumping headlines to console size=$size")
    source.runWith(consoleSink)
  }

  def receive(): Receive = {
    case (headlines:Seq[Headline]) => consoleDump(headlines)
    case _ => log.error("unknown message received")
  }
}
