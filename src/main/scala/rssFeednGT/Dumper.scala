package rssFeednGT

import akka.{Done, NotUsed}
import akka.actor.{Actor, ActorLogging, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Failure, Success}

class Dumper extends Actor with ActorLogging {
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val consoleSink: Sink[(Headline, Int), Future[Done]] = Sink.foreach[(Headline, Int)] {x =>
    println(x._1)
    println(x._2)
  }

  def trendingMeasured(trends: List[String]): Flow[Headline, (Headline, Int), NotUsed] =
    Flow[Headline].mapAsync(2) { headline =>
      Future {
        (headline, headline.trendingMeasure(trends))
      }
    }

  val inTrends: Flow[(Headline, Int), (Headline, Int), NotUsed] = Flow[(Headline, Int)].filter(_._2 != 0)

  def consoleDump(headlines:Seq[Headline]): Unit = {
    implicit val timeout: Timeout = Timeout(30.seconds)
    val source = Source.fromIterator(() => headlines.iterator)
    (context.actorOf(Props[GoogleTrends]) ? ()).mapTo[List[String]].onComplete {
      case Success(trends) => source.via(trendingMeasured(trends)).via(inTrends).runWith(consoleSink)
      case Failure(exception) => log.error(s"failed to get trends ex = $exception")
    }
  }

//  def trendingMeasure(headline: Headline, trends: List[String]): Int = {
//    val text = headline.description + headline.title
//    trends map {trend => countOccurrences(text,trend)} sum
//  }
//
//  def countOccurrences(src: String, tgt: String): Int =
//    src.toSeq.sliding(tgt.length).map(_.unwrap).count(window => window == tgt)

  def receive(): Receive = {
    case (headlines:Seq[Headline]) => consoleDump(headlines)
    case _ => log.error("unknown message received")
  }
}
