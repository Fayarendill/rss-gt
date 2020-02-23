package rssFeednGT

import com.rometools.rome.feed.synd.SyndFeed
import monix.reactive.Observable

object Main extends App {
  val aggregator: Observable[Aggregator] = Config.prefs.scan(new Aggregator(new Config.Prefs)) {
    (agg, prefs) => agg.cancel; new Aggregator(prefs)
  }

  Config.load()

//  val aggregator: Aggregator = new Aggregator(new Config.Prefs)
//
//  val testFeed: (String, SyndFeed) = aggregator.download("http://rss.cnn.com/rss/edition.rss")

  Config.watcher.cancel
}
