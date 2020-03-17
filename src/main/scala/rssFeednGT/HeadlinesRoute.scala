package rssFeednGT

import akka.NotUsed
import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.server.Directives.{complete, get, path}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.scaladsl.Source
import akka.util.Timeout
import rssFeednGT.JsonFormats._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}

object HeadlinesRoute {
  implicit val timeout: Timeout = Timeout(5.seconds)
  implicit val jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json()

  def getHeadlines(system: ActorSystem): Future[Source[(HeadlineC, String), NotUsed]] = {
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher
    val dumper = system.actorOf(Props[Dumper])
    (dumper ? DumperToOut()).mapTo[Future[Source[(HeadlineC, String), NotUsed]]].flatten
  }

  def headlinesRoute(system: ActorSystem): Route =
    path("headlines") {
      get {
        complete(getHeadlines(system))
      }
    }
}

