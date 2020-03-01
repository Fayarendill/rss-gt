package rssFeednGT

import java.io.ByteArrayInputStream

import akka.actor.{Actor, ActorLogging}
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.{SyndFeedInput, XmlReader}
import scalaj.http.{Http, HttpResponse}

import scala.jdk.CollectionConverters._

class GoogleTrends extends Actor with ActorLogging {
  val trendsUrl = "https://trends.google.com/trends/trendingsearches/daily/rss?geo=US"

  def receive(): Receive = {
    case () => sender ! trends(trendsUrl)
  }

  def trends(url: String, redirects: Int = 5): List[String] = {
  Http(url).timeout(5000, 10000).asBytes match {
    case r if r.isRedirect && redirects > 0 =>
      trends(r.location.get, redirects-1)
    case r if !r.isSuccess =>
      throw new Exception(r.statusLine)
    case r =>
      val input = new ByteArrayInputStream(r.body)
      val feed = new SyndFeedInput().build(new XmlReader(input))
      log.info(s"downloaded trends, url = $url")
      extractTrends(feed)
    }
  }

  def extractTrends(feed: SyndFeed): List[String] = {
    feed.getEntries.asScala.toList flatMap { entry =>
      Seq(entry.getTitle, entry.getDescription.getValue)
    } filterNot (_.isEmpty) flatMap { trend =>
      trend.split(", ").toList
    }
  }

}


