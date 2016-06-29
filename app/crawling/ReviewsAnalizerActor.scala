package crawling

import akka.actor.{Actor, PoisonPill, Props}
import models.{Competitor, Review}
import org.joda.time.{DateTime, LocalDate}
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import play.api.libs.ws.WSResponse
import org.jsoup.Jsoup

/**
  * Created by borisbondarenko on 18.06.16.
  */
object ReviewsAnalizerActor {

  trait Factory {
    def apply(): Actor
  }

  def props = Props[ReviewsAnalizerActor]

  case class AnalizeReviews(competitorId: Option[Long], reviews: WSResponse)
  case class AnalizeReviewsComplete(competitorId: Option[Long], reviews: Seq[Review])
}

class ReviewsAnalizerActor extends Actor {

  import ReviewsAnalizerActor._
  import scala.collection.JavaConversions._

  val formatter: DateTimeFormatter = DateTimeFormat.forPattern("dd.MM.yyyy")

  override def receive: Receive = {
    case AnalizeReviews(cmp, reviews) =>
      val r = Jsoup.parse(reviews.bodyAsUTF8)
      val result = (
        r.select("div.grid-content.wordwrap.text-desc > a").map(_.text),
        r.select("div.grid-550.list-item-content > div.grid-auto.gray[title]").map(_.text),
        r.select("div.grid-530").map(_.text)).zipped.toSeq
      .map { el => Review(None, cmp, el._1, el._3, LocalDate.parse(el._2, formatter)) }

      val list = result.toList

      sender ! AnalizeReviewsComplete(cmp, result)
      self ! PoisonPill
  }
}
