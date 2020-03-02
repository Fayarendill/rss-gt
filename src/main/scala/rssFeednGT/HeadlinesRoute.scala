package rssFeednGT

import akka.NotUsed
import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
//import akka.http.scaladsl.common.EntityStreamingSupport
//import akka.http.scaladsl.marshallers.sprayjson.JsonEntityStreamingSupport
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives.{complete, get, path}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.scaladsl.Source
import akka.util.{ByteString, Timeout}

import scala.collection.immutable
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import JsonFormats._


//import scala.concurrent.ExecutionContext.Implicits.global

object HeadlinesRoute {
  implicit val timeout: Timeout = Timeout(30.seconds)
  implicit val jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json().withParallelMarshalling(parallelism = 8, unordered = false)

  def getHeadlines(system: ActorSystem) = {
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher
//    val fetcher = system.actorOf(Props[Fetcher])
    val dumper  = system.actorOf(Props[Dumper])
    for {
      source <- (dumper ? ()).mapTo[Source[HeadlineC, NotUsed]]
    } yield source

  }

  def headlinesRoute(system: ActorSystem): Route =
    path("headlines") {
      get {
        complete(getHeadlines(system))
      }
    }
}
