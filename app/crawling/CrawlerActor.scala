package crawling

import javax.inject.{Inject, Named}

import akka.actor._
import akka.stream.Materializer
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
    def apply(): Actor
  }

  def props = Props[CrawlerActor]

  case class CrawlCompetitor(competitor: Competitor)
  case object CrawlComplete
}

class CrawlerActor @Inject()(
    @Named("persister") persisterActor: ActorRef,
    reviewsAnalizersFactory: ReviewsAnalizerActor.Factory,
    goodsAnalizersFactory: GoodsAnalizerActor.Factory,
    implicit val mat: Materializer) extends Actor with InjectedActorSupport {

  import CrawlerActor._
  import ReviewsAnalizerActor._
  import context.dispatcher
  import org.jsoup.Jsoup

  import scala.collection.JavaConversions._

  private var isReviewReady: Boolean = false
  private var isGoodsReady: Boolean = false
  private val httpClient: AhcWSClient = AhcWSClient()

  @scala.throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    httpClient.close()
    super.postStop()
  }

  override def receive: Receive = {
    case CrawlCompetitor(c) =>
      for {
        main <- httpClient.url(s"${c.url}/?sortitems=4&v=0").get()
        firstReviews <- httpClient.url(s"${c.url}/feedbacks").get()
        goods <- getGoodsPages(c, main)
        reviews <- getReviewsPages(c, firstReviews)
      } yield {
        // analize reviews
        reviews.zipWithIndex.foreach { case(r, idx) =>
          val name = s"reviews-analizer-${c.id.getOrElse(0)}-$idx-${System.nanoTime}"
          val analizerActor = injectedChild(reviewsAnalizersFactory(), name)
          analizerActor ! AnalizeReviews(c, r)

          Logger.info(s"Analize reviews $name")
        }

        // analize goods
        goods.zipWithIndex.foreach { case(g, idx) =>
          val name = s"goods-analizer-${c.id.getOrElse(0)}-$idx-${System.nanoTime}"
          val analizerActor = injectedChild(goodsAnalizersFactory(), name)
          analizerActor ! AnalizeGoods(c, g)

          Logger.info(s"Analize goods $name")
        }

        // analize subscribers
        val subscribersAmount = Jsoup.parse(main.bodyAsUTF8).select("#totalSubscribers").text.toInt
        persisterActor ! UpdateAmount(c.id.get, subscribersAmount, LocalDate.now)
      }

    case AnalizeReviewsComplete(cmp, reviews) =>
      persisterActor ! UpdateReviews(reviews)
      isReviewReady = true
      sendCompleteIfReady()

    case AnalizeGoodsComplete(cmp, goods) =>
      persisterActor ! UpdateGoods(goods)
      isGoodsReady = true
      sendCompleteIfReady()
  }

  private def sendCompleteIfReady(): Unit =
    if(isGoodsReady && isReviewReady) {
      context.parent ! CrawlComplete
      self ! PoisonPill
      Logger.info(s"PoisonPill $self")
    }

  private def getGoodsPages(c: Competitor, main: WSResponse): Future[Seq[WSResponse]] = {
    val pageCount = getPagesCount(main)
    val goods = (0 to (pageCount - c.crawledGoodsPages max 0)).map { i =>
      httpClient.url(s"${c.url}?sortitems=0&v=0&from=${40 * i}").get()
    }

    Future.sequence(goods)
  }

  private def getReviewsPages(c: Competitor, rvws: WSResponse): Future[Seq[WSResponse]] = {
    val pageCount = getPagesCount(rvws)
    val reviews = (0 to (pageCount - c.crawledReviewsPages max 0)).map { i =>
      httpClient.url(s"${c.url}/feedbacks?status=m&from=${40 * i}").get()
    }

    Future.sequence(reviews)
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