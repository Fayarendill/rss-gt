package rssFeednGT

import java.nio.file.Paths

import akka.actor.{Actor, ActorLogging}
import org.json4s._
import org.json4s.native.JsonMethods._

import scala.io.Source

case class Subscriptions (
                         urls: Seq[String] = List.empty
                         )

class SubscriptionReader extends Actor with ActorLogging{
  def load(filename:String): JValue = {
    val file = List("HOME", "USERPROFILE")
      .flatMap(env => Option(System getenv env))
      .headOption
      .map(Paths.get(_))
      .getOrElse(Paths.get("/"))
      .resolve(filename)

    // load the source file and parse it as JSON
    val source: String = Source.fromFile(file.toFile).mkString
    parse(source)
  }
  def read(jval:JValue): Seq[String] = {
    implicit val formats: DefaultFormats.type = DefaultFormats // Brings in default date formats etc.
    val urls = jval.extract[Subscriptions].urls
    urls
  }

  def receive(): Receive = {
    case filename:String => sender ! read(load(filename))
  }
}
