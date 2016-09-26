package crawling.`new`

import com.zebrosoft.crystal.model.Competitor
import crawling.GoodsAnalizerActor.AnalizeGoods
import crawling.PersisterActor.UpdateAmount
import crawling.ReviewsAnalizerActor.AnalizeReviews
import org.joda.time.LocalDate
import org.jsoup.Jsoup
import play.api.Logger
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.Future

/**
  * Created by borisbondarenko on 26.09.16.
  */
object Crawler {

  def crawlCompetitor(c: Competitor): Future[Unit] = {

    val httpClient: AhcWSClient = AhcWSClient()

    val mainFuture = httpClient.url(s"${c.url}/?sortitems=4&v=0").get()
    val firstReviewsFuture = httpClient.url(s"${c.url}/feedbacks").get()

    for {
      main <- mainFuture
      firstReviews <- firstReviewsFuture
      goods <- getPages(main, c.crawledGoodsPages, c.url, "?sortitems=0&v=0&from=")
      reviews <- getPages(firstReviews, c.crawledReviewsPages, c.url, "/feedbacks?status=m&from=")
    } yield {

      Logger.info(s"Goods Pages Count -- ${goods.size}")
      Logger.info(s"Reviews Pages Count -- ${reviews.size}")

      val reviewsActors =
        reviews.zipWithIndex.map { case(r, idx) =>
          val name = s"reviews-analizer-${c.id.getOrElse(0)}-$idx-${System.nanoTime}"
          (injectedChild(reviewsAnalizersFactory(), name), r)
        }

      val goodsActors =
        goods.zipWithIndex.map { case(g, idx) =>
          val name = s"goods-analizer-${c.id.getOrElse(0)}-$idx-${System.nanoTime}"
          (injectedChild(goodsAnalizersFactory(), name), g)
        }

      waitingForReviews ++= reviewsActors.map(_._1)
      waitingForGoods ++= goodsActors.map(_._1)

      reviewsActors.foreach(actor => actor._1 ! AnalizeReviews(c.id, actor._2))
      goodsActors.foreach(actor => actor._1 ! AnalizeGoods(c.id, actor._2))

      // analize subscribers
      val subscribersAmount = Jsoup.parse(main.bodyAsUTF8).select("#totalSubscribers").text.toInt
      getPersisterActor ! UpdateAmount(c.id.get, subscribersAmount, LocalDate.now)
    }
  }
}
