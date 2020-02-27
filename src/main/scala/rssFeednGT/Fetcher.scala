package rssFeednGT

import java.io.ByteArrayInputStream

import akka.actor.{Actor, ActorLogging}
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.{SyndFeedInput, XmlReader => RXmlReader}
import scalaj.http.Http

import scala.jdk.CollectionConverters._

class Fetcher extends Actor with ActorLogging {
//  val cache: mutable.Map[String, Headline] = mutable.Map[String, Headline]()

//  def download(url: String, redirects: Int = 5): List[Headline] = {
//    Http(url).timeout(5000, 10000).asBytes match {
//      case r if r.isRedirect && redirects > 0 =>
//        download(r.location.get, redirects-1)
//      case r if !r.isSuccess =>
//        throw new Exception(r.statusLine)
//      case r =>
//        val input = new ByteArrayInputStream(r.body)
//        val feed = new SyndFeedInput().build(new RXmlReader(input))
//
//        log.info(url)
////        val source : Source[Headline, NotUsed] = Source.fromIterator(() => headlines(feed))
////        source
//        extractHeadlines(feed)
//    }
//  }
  def download(url: String, redirects: Int = 5): List[Headline] = {
    Http(url).timeout(5000, 10000).asBytes match {
      case r if r.isRedirect && redirects > 0 =>
        download(r.location.get, redirects-1)
      case r if !r.isSuccess =>
        throw new Exception(r.statusLine)
      case r =>
        val input = new ByteArrayInputStream(r.body)
        val feed = new SyndFeedInput().build(new RXmlReader(input))

        log.info(url)
//        Source.fromIterator (() => extractHeadlines(feed))
        extractHeadlines(feed)
    }
  }
  def receive(): Receive = {
    case url:String => sender ! download(url)
    case urls:Seq[String] => sender ! urls.flatMap {url => download(url)}
    case _ => log.error("unknown message received")
//    case () => sender ! Source.fromIterator(() => cache.valuesIterator)
  }

  def headlines(feed: SyndFeed): Iterator[Headline] = {
    feed.getEntries.asScala.iterator flatMap { entry =>
      Seq(new Headline(feed, entry))
    }
  }

//  def extractHeadlines(feed: SyndFeed): List[Headline] = {
//    feed.getEntries.asScala.toList flatMap { entry =>
//      Try(cache getOrElseUpdate (entry.getUri, new Headline(feed, entry))).toOption
//    }
//  }

  def extractHeadlines(feed: SyndFeed): List[Headline] = {
    feed.getEntries.asScala.toList flatMap { entry =>
      Seq(new Headline(feed, entry))
    }
  }
}
