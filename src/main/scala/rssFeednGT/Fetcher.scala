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
  def receive(): Receive = {
    case FetcherAddUrl(url) => Fetcher.addUrl(url)
    case msg:FetcherGetHeadlines => sender ! Fetcher.getHeadlines.valuesIterator.toList
    case msg:FetcherReload => reload()
    case _ => log.error("unknown message received")
  }

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

  def reload(): mutable.Set[List[Headline]] = {
    Fetcher.cleanCache()
    Fetcher.getUrls.map(download(_))
  }

  def extractHeadlines(feed: SyndFeed): List[Headline] = {
    feed.getEntries.asScala.toList flatMap { entry =>
      Try(Fetcher.updateHeadlines(entry.getUri, new Headline(feed, entry))).toOption
    }
  }

}

object Fetcher {
  private val headlinesCache: mutable.Map[String, Headline] = mutable.Map[String, Headline]()
  private val rssUrls: mutable.Set[String] = mutable.Set[String]()
  def getHeadlines: mutable.Map[String, Headline] = headlinesCache
  def updateHeadlines(url:String, headline: Headline): Headline = headlinesCache getOrElseUpdate(url, headline)
  def cleanCache(): Unit = headlinesCache.clear()
  def getUrls: mutable.Set[String] = rssUrls
  def addUrl(url: String) = rssUrls += url
}

case class FetcherGetHeadlines()
case class FetcherAddUrl(url: String)
case class FetcherReload()