package crawling

import javax.inject.{Inject, Named}

import akka.actor._
import akka.stream.Materializer
import com.google.inject.assistedinject.Assisted
import crawling.GoodsAnalizerActor.{AnalizeGoods, AnalizeGoodsComplete}
import crawling.PersisterActor.{UpdateAmount, UpdateGoods, UpdateReviews}
import models.Competitor
import org.joda.time.LocalDate
import play.api.Logger
import play.api.libs.concurrent.InjectedActorSupport
import play.api.libs.ws.WSResponse
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.Future

/**
  * Created by borisbondarenko on 04.06.16.
  */
object CrawlerActor {

  trait Factory {
    def apply(c: Competitor): Actor
  }

  def props = Props[CrawlerActor]

  case object CrawlCompetitor
  case object CrawlComplete
}

class CrawlerActor @Inject()(
    @Assisted c: Competitor,
    @Named("persister") persisterActor: ActorRef,
    reviewsAnalizersFactory: ReviewsAnalizerActor.Factory,
    goodsAnalizersFactory: GoodsAnalizerActor.Factory,
    implicit val mat: Materializer) extends Actor with InjectedActorSupport {

  import CrawlerActor._
  import ReviewsAnalizerActor._
  import context.dispatcher
  import org.jsoup.Jsoup

  import scala.collection.JavaConversions._

  private val httpClient: AhcWSClient = AhcWSClient()
  private var waitingFor = Set.empty[ActorRef]

  @scala.throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    waitingFor = Set.empty
    httpClient.close()
    super.postStop()
  }

  override def receive: Receive = {
    case CrawlCompetitor =>
      for {
        main <- httpClient.url(s"${c.url}/?sortitems=4&v=0").get()
        firstReviews <- httpClient.url(s"${c.url}/feedbacks").get()
        goods <- getPages(main, c.crawledGoodsPages, "?sortitems=0&v=0&from=")
        reviews <- getPages(firstReviews, c.crawledReviewsPages, "/feedbacks?status=m&from=")
      } yield {

        waitingFor ++=
          reviews.zipWithIndex.map { case(r, idx) =>
            val name = s"reviews-analizer-${c.id.getOrElse(0)}-$idx-${System.nanoTime}"
            val analizerActor = injectedChild(reviewsAnalizersFactory(), name)
            analizerActor ! AnalizeReviews(c, r)

            Logger.info(s"Analize reviews $name")
            analizerActor
          } ++
          goods.zipWithIndex.map { case(g, idx) =>
            val name = s"goods-analizer-${c.id.getOrElse(0)}-$idx-${System.nanoTime}"
            val analizerActor = injectedChild(goodsAnalizersFactory(), name)
            analizerActor ! AnalizeGoods(c, g)

            Logger.info(s"Analize goods $name")
            analizerActor
          }

        // analize subscribers
        val subscribersAmount = Jsoup.parse(main.bodyAsUTF8).select("#totalSubscribers").text.toInt
        persisterActor ! UpdateAmount(c.id.get, subscribersAmount, LocalDate.now)
      }

    case AnalizeReviewsComplete(cmp, reviews) =>
      persisterActor ! UpdateReviews(reviews)
      sendCompleteIfReady(sender)

    case AnalizeGoodsComplete(cmp, goods) =>
      persisterActor ! UpdateGoods(goods)
      sendCompleteIfReady(sender)
  }

  private def sendCompleteIfReady(sndr: ActorRef): Unit =
    waitingFor -= sender
    if(waitingFor.isEmpty) {
      context.parent ! CrawlComplete
      self ! PoisonPill
      Logger.info(s"PoisonPill $self")
    }

  private def getPages(mainPage: WSResponse, pagesCount: Int, url: String): Future[Seq[WSResponse]] = {
    val pageCount = getPagesCount(mainPage)
    Future.sequence {
      (0 to (pageCount - pagesCount max 0)).map { i =>
        httpClient.url(s"${c.url}$url${40 * i}").get()
      }
    }
  }

  private def getPagesCount(resp: WSResponse): Int =
    Jsoup.parse(resp.bodyAsUTF8).select("tbody > tr > td > a") match {
      case x if x.isEmpty => 0
      case x =>
        x.map(_.text)
          .map(_.replaceAll("[^\\d.]", ""))
          .filterNot(_.isEmpty)
          .map(_.toInt)
          .max
    }
}