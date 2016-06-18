package crawling

import akka.actor.{Actor, Props}
import models.Competitor
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

  case class AnalizeReviews(competitor: Competitor, reviews: WSResponse)
  case class AnalizeReviewsComplete()
}

class ReviewsAnalizerActor extends Actor {

  import ReviewsAnalizerActor._
  import scala.collection.JavaConversions._

  override def receive: Receive = {
    case AnalizeReviews(cmp, reviews) =>
      val r = Jsoup.parse(reviews.bodyAsUTF8)
      val result =
        r.select("div.grid-content.wordwrap.text-desc > a").map(_.text) zip
        r.select("div.grid-550.list-item-content > div.grid-auto.gray[title]").map(_.text) zip
        r.select("div.grid-530").map(_.text)

      sender ! AnalizeReviewsComplete

  }
}
