package rssFeednGT

import akka.NotUsed
import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.marshalling.{Marshaller, Marshalling}
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.server.Directives.{complete, get, path}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.scaladsl.{Flow, Source}
import akka.util.{ByteString, Timeout}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import JsonFormats._

object HeadlinesRoute {
  implicit val timeout: Timeout = Timeout(5.seconds)
  implicit val jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json()

  implicit val stringFormat: Marshaller[String, ByteString] = Marshaller[String, ByteString] { ec => s =>
    Future.successful {
      List(Marshalling.WithFixedContentType(ContentTypes.`application/json`, () =>
        ByteString("\"" + s + "\"")) // "raw string" to be rendered as json element in our stream must be enclosed by ""
      )
    }
  }

  def getHeadlines(system: ActorSystem) = {
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher
    val toStringF: Flow[HeadlineC, String, NotUsed] = Flow[HeadlineC].mapAsync(2) { x =>
      Future {
        x.title + " " + x.body
      }
    }

    val dumper = system.actorOf(Props[Dumper])
    (dumper ? DumperToOut()).mapTo[Future[Source[HeadlineC, NotUsed]]].flatten.map(_.via(toStringF))
  }

  def headlinesRoute(system: ActorSystem): Route =
    path("headlines") {
      get {
        complete(getHeadlines(system))
      }
    }
}

