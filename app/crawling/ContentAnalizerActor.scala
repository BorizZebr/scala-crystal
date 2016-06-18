package crawling

import akka.actor.{Actor, Props}
import models.Competitor
import play.api.libs.ws.WSResponse
import org.jsoup.Jsoup
import scala.collection.JavaConversions._

/**
  * Created by borisbondarenko on 18.06.16.
  */
object ContentAnalizerActor {

  trait Factory {
    def apply(): Actor
  }

  def props = Props[ContentAnalizerActor]

  case class AnalizeContent(competitor: Competitor, goods: Seq[WSResponse], reviews: Seq[WSResponse])

  case class AnalizeComplete()
}

class ContentAnalizerActor extends Actor {

  import ContentAnalizerActor._

  override def receive: Receive = {
    case AnalizeContent(cmp, goods, reviews) =>

      val subscribers = Jsoup.parse(goods.head.body).select("#totalSubscribers").text() toInt

      val parsedGoods = goods.map(g => Jsoup.parse(g.bodyAsUTF8))
      val parsedReviews = reviews
        .map(r => Jsoup.parse(r.bodyAsUTF8))
        .flatMap { r =>
          r.select("div.grid-content.wordwrap.text-desc > a").map(_.text)
        }

      sender ! AnalizeComplete

  }
}
