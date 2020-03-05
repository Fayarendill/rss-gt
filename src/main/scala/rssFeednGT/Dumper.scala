package rssFeednGT

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.Timeout
import akka.{Done, NotUsed}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class Dumper extends Actor with ActorLogging {
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val consoleSink: Sink[(Headline, (Int, String)), Future[Done]] = Sink.foreach[(Headline, (Int, String))] {x =>
    println(x._1)
    println(x._2)
  }

  def trendingMeasured(trends: List[Trend]): Flow[Headline, (Headline, (Int, String)), NotUsed] =
    Flow[Headline].mapAsync(2) { headline =>
      Future {
        headline -> headline.trendingMeasure(trends)
      }
    }

  val inTrends: Flow[(Headline, (Int, String)), (Headline, (Int, String)), NotUsed] = Flow[(Headline, (Int, String))].filter(_._2._1 != 0)

  val outFlow: Flow[(Headline, (Int, String)), (HeadlineC, String), NotUsed] = Flow[(Headline, (Int, String))].mapAsync(2) { x =>
    Future {
      x._1.toOut -> x._2._2
    }
  }

  def consoleDump(headlines:Seq[Headline]): Unit = {
    implicit val timeout: Timeout = Timeout(30.seconds)
    val source = Source.fromIterator(() => headlines.iterator)
    (context.actorOf(Props[GoogleTrends]) ? ()).mapTo[List[Trend]].onComplete {
      case Success(trends) => source.via(trendingMeasured(trends)).via(inTrends).runWith(consoleSink)
      case Failure(exception) => log.error(s"failed to get trends ex = $exception")
    }
  }

  def outDump(): Future[Source[(HeadlineC, String), NotUsed]] = {
    implicit val timeout: Timeout = Timeout(30.seconds)
    val fetcher      = context.actorOf(Props[Fetcher])
    val googleTrends = context.actorOf(Props[GoogleTrends])
    for {
      headlines <- (fetcher ? FetcherGetHeadlines()).mapTo[Seq[Headline]]
      trends <- (googleTrends ? ()).mapTo[List[Trend]]
    } yield Source.fromIterator(() => headlines.iterator).via(trendingMeasured(trends)).via(inTrends).via(outFlow)
  }

  def receive(): Receive = {
    case DumperToConsole(headlines) => consoleDump(headlines)
    case msg:DumperToOut => sender ! outDump()
    case _ => log.error("unknown message received")
  }
}

case class DumperToConsole(headlines: Seq[Headline])
case class DumperToOut()