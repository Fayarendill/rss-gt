package rssFeednGT

import com.rometools.rome.feed.synd.{SyndContent, SyndEntry, SyndFeed}
import java.util.Date
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist
import scala.jdk.CollectionConverters._
import scala.language.postfixOps
import scala.util.matching.Regex

class Headline(val feed: SyndFeed, val entry: SyndEntry) extends Comparable[Headline] {
  /** Clean and parse the title as HTML.
   */
  val title: String = Jsoup.parse(Jsoup.clean(entry.getTitle.replace('\n', ' '), Whitelist.none)).text

  /** Get the optional body of the Headline. Then parse parse and clean
   * the HTML inside it as a summary.
   */
  val contents: Option[SyndContent] = entry.getContents.asScala.headOption
  val description: String = contents orElse Option(entry.getDescription) map (_.getValue) getOrElse ""
  val summary: String = Jsoup.clean(description, Whitelist.relaxed)
  val body: String = Jsoup.parse(summary).body.text

  /** Get the published date of this Headline. If there is no updated date set,
   * use the published date of the feed. If that isn't set, use the date now.
   */
  val date: Date = Option(entry.getUpdatedDate)
    .orElse(Option(entry.getPublishedDate))
    .getOrElse(new Date)

  def toOut: HeadlineC = HeadlineC(this.title, this.body)

  def trendingMeasure(trends: List[Trend]): Int = {
    val nonWord: Regex = "\\W".r
    val text           = nonWord.replaceAllIn((this.body + this.title).map (_.toLower), "")
    trends map {trend => countOccurrences(text,trend.text)} sum
  }

  def countOccurrences(src: String, tgt: String): Int =
    src.toSeq.sliding(tgt.length).map(_.unwrap).count(window => window == tgt)

  /** True if a Headline belongs to a given feed. This exists since Headlines
   * are cached and the feed object can change.
   */
  def belongsTo(f: SyndFeed): Boolean =
    f == feed || ((f.getUri, feed.getUri) match {
      case (null, _) | (_, null) => f.getLink == feed.getLink
      case (a, b)                => a == b
    })

  /** True if this headline is the same as another.
   */
  def matchesHeadline(h: Headline): Boolean =
    (entry.getUri == h.entry.getUri) || (entry.getLink == h.entry.getLink)

  /** Show the title of the Headline.
   */
  override def toString = s"$title"

  /** Headlines are the same if they resolve to the same end-point.
   */
  override def equals(obj: Any): Boolean =
    obj match {
      case h: Headline => matchesHeadline(h)
      case s: String   => entry.getLink == s
      case _           => false
    }

  /** Headlines are sorted by date and then by title.
   */
  override def compareTo(h: Headline): Int =
    h.date compareTo date match {
      case 0 => entry.getTitle compareTo h.entry.getTitle
      case c => c
    }

  /** Hash by link.
   */
  override def hashCode: Int = entry.getLink.hashCode
}

case class HeadlineC(title: String, body: String)