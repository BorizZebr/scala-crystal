package crawling

import akka.actor.{Actor, PoisonPill, Props}
import models.{Competitor, Good}
import org.joda.time.{DateTime, LocalDate}
import org.jsoup.Jsoup
import play.api.libs.ws.WSResponse

/**
  * Created by borisbondarenko on 18.06.16.
  */
object GoodsAnalizerActor {

  trait Factory {
    def apply(): Actor
  }

  def props = Props[GoodsAnalizerActor]

  case class AnalizeGoods(competitor: Competitor, goods: WSResponse)
  case class AnalizeGoodsComplete(competitor: Competitor, goods: Seq[Good])
}

class GoodsAnalizerActor extends Actor {

  import GoodsAnalizerActor._
  import scala.collection.JavaConversions._

  override def receive: Receive = {
    case AnalizeGoods(cmp, goods) =>
      val g = Jsoup.parse(goods.bodyAsUTF8)
      val res = g.select("div.b-item.b-item-hover").map { el => (
        100500,
        el.select("div.title > a").text,
        el.select("span.price").text,
        el.select("img").attr("src"),
        "https://www.livemaster.ru/" + el.select("a").attr("href"))
      }. map { el =>
        Good(None, cmp.id, el._1, el._2, el._3.split(" ").head.toDouble, el._4, el._5, LocalDate.now)
      }

      sender ! AnalizeGoodsComplete(cmp, res)
      self ! PoisonPill
  }
}
