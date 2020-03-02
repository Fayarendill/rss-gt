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

  val consoleSink: Sink[(Headline, Int), Future[Done]] = Sink.foreach[(Headline, Int)] {x =>
    println(x._1)
    println(x._2)
  }

  def trendingMeasured(trends: List[Trend]): Flow[Headline, (Headline, Int), NotUsed] =
    Flow[Headline].mapAsync(2) { headline =>
      Future {
        (headline, headline.trendingMeasure(trends))
      }
    }

  val inTrends: Flow[(Headline, Int), (Headline, Int), NotUsed] = Flow[(Headline, Int)].filter(_._2 != 0)

  val outFlow: Flow[(Headline, Int), String, NotUsed] = Flow[(Headline, Int)].mapAsync(2) { x =>
    Future {
      x._1.title
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

  def outDump(): Future[Source[String, NotUsed]] = {
    implicit val timeout: Timeout = Timeout(30.seconds)
    val fetcher      = context.actorOf(Props[Fetcher])
    val googleTrends = context.actorOf(Props[GoogleTrends])
    for {
      headlines <- (fetcher ? ()).mapTo[Seq[Headline]]
      trends <- (googleTrends ? ()).mapTo[List[Trend]]
    } yield Source.fromIterator(() => headlines.iterator).via(trendingMeasured(trends)).via(inTrends).via(outFlow)
  }

  def receive(): Receive = {
    case headlines:Seq[Headline] => consoleDump(headlines)
    case () => sender ! outDump()
    case _ => log.error("unknown message received")
  }
}
