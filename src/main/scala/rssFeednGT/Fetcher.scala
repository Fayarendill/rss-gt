package rssFeednGT

import java.io.ByteArrayInputStream

import akka.actor.{Actor, ActorLogging}
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.{SyndFeedInput, XmlReader}
import scalaj.http.Http

import scala.collection.mutable
import scala.jdk.CollectionConverters._
import scala.util.Try

class Fetcher extends Actor with ActorLogging {
  def download(url: String, redirects: Int = 5): List[Headline] = {
    Http(url).timeout(5000, 10000).asBytes match {
      case r if r.isRedirect && redirects > 0 =>
        download(r.location.get, redirects-1)
      case r if !r.isSuccess =>
        throw new Exception(r.statusLine)
      case r =>
        val input = new ByteArrayInputStream(r.body)
        val feed = new SyndFeedInput().build(new XmlReader(input))

        log.info(s"downloaded feed, url = $url")
        extractHeadlines(feed)
    }
  }

  def receive(): Receive = {
    case url:String => sender ! download(url)
    case urls:Seq[String] => sender ! urls.flatMap {url => download(url)}
    case () => sender ! Fetcher.getHeadlines.valuesIterator.toList
    case _ => log.error("unknown message received")
  }

  def headlines(feed: SyndFeed): Iterator[Headline] = {
    feed.getEntries.asScala.iterator flatMap { entry =>
      Seq(new Headline(feed, entry))
    }
  }

  def extractHeadlines(feed: SyndFeed): List[Headline] = {
    feed.getEntries.asScala.toList flatMap { entry =>
      Try(Fetcher.updateHeadlines(entry.getUri, new Headline(feed, entry))).toOption
    }
  }

}

object Fetcher {
  private val headlinesCache: mutable.Map[String, Headline] = mutable.Map[String, Headline]()
  def getHeadlines: mutable.Map[String, Headline] = headlinesCache
  def updateHeadlines(url:String, headline: Headline): Headline = headlinesCache getOrElseUpdate(url, headline)
}