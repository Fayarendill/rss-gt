package rssFeednGT

import java.awt.Desktop
import java.io.PrintWriter
import java.nio.file._

import monix.reactive.subjects._
import org.joda.time.format.{PeriodFormatter, PeriodFormatterBuilder}
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization.write

import scala.io.Source
import scala.util._

object Config {
  implicit val formats: Formats = DefaultFormats

  /** Options saved to a JSON file.
   */
  case class Prefs(
                    val urls: List[String] = List.empty,
                    val filters: List[String] = List.empty,
                  )

  /** Whenever the preferences are loaded, send them to this observable.
   */
  val prefs: PublishSubject[Prefs] = PublishSubject[Prefs]()

  /** Where are the config files located.
   */
  val home: Path = List("HOME", "USERPROFILE")
    .flatMap(env => Option(System getenv env))
    .headOption
    .map(Paths.get(_))
    .getOrElse(Paths.get("/"))

  /** Find the user's HOME path and the preferences file within it.
   */
  val file: Path = home.resolve("rss.json")

  /** Create a file watcher on the preferences file.
   */
  val watcher = new Watcher(file)(load _)

  /** Load the file and publish the new preferences.
   */
  def load(): Unit = {
    scribe info "Reloading preferences..."

    // load the source file and parse it as JSON
    if (Files.exists(file) && Files.isRegularFile(file)) {
      val source: String = Source.fromFile(file.toFile).mkString

      // update the preferences
      Try(parse(source)) foreach {
        json => Option(json.extract[Prefs]) foreach prefs.onNext
      }
    }
  }

  /** Launch the default editor and let the user modify the preferences.
   */
  def open: Unit = {
    if (Try(Desktop.getDesktop open file.toFile).isFailure) {
      val writer = new PrintWriter(file.toFile)

      // create the default preferences file
      writer.write(write(new Prefs))
      writer.close

      // try again
      open
    }
  }

  watcher.start
}
