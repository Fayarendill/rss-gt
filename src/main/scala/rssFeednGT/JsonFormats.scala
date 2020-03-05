package rssFeednGT

import spray.json.RootJsonFormat

object JsonFormats
  extends akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
    with spray.json.DefaultJsonProtocol {

  implicit val headlineСJsonFormat: RootJsonFormat[HeadlineC] = jsonFormat3(HeadlineC.apply)

}