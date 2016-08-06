package crawling

import javax.inject.Inject

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

  case class CrawlCompetitor(c: Competitor)
}

class CrawlerActor @Inject()(
    reviewsAnalizersFactory: ReviewsAnalizerActor.Factory,
    goodsAnalizersFactory: GoodsAnalizerActor.Factory,
    persisterFactory: PersisterActor.Factory,
    implicit val mat: Materializer) extends Actor with InjectedActorSupport {

  import CrawlMasterActor._
  import CrawlerActor._
  import ReviewsAnalizerActor._
  import context.dispatcher
  import org.jsoup.Jsoup

  import scala.collection.JavaConversions._

  private val httpClient: AhcWSClient = AhcWSClient()
  private var waitingForReviews = Set.empty[ActorRef]
  private var waitingForGoods = Set.empty[ActorRef]

  @scala.throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    waitingForReviews = Set.empty
    waitingForGoods = Set.empty

    httpClient.close()
    super.postStop()
  }

  override def receive: Receive = {
    case CrawlCompetitor(c) =>

      for {
        main <- httpClient.url(s"${c.url}/?sortitems=4&v=0").get()
        firstReviews <- httpClient.url(s"${c.url}/feedbacks").get()
        goods <- getPages(main, c.crawledGoodsPages, c.url, "?sortitems=0&v=0&from=")
        reviews <- getPages(firstReviews, c.crawledReviewsPages, c.url, "/feedbacks?status=m&from=")
      } yield {

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

    case AnalizeReviewsComplete(cmp, reviews) =>
      getPersisterActor ! UpdateReviews(reviews)
      waitingForReviews -= sender
      sendCompleteIfReady()

    case AnalizeGoodsComplete(cmp, goods) =>
      getPersisterActor ! UpdateGoods(goods)
      waitingForGoods -= sender
      sendCompleteIfReady()
  }

  private def getPersisterActor: ActorRef = {
    val name = s"persister-${System.nanoTime}"
    injectedChild(persisterFactory(), name)
  }

  private def sendCompleteIfReady(): Unit =
    if (waitingForReviews.isEmpty && waitingForGoods.isEmpty) {
      context.parent ! CrawlComplete
      self ! PoisonPill
      Logger.info(s"PoisonPill $self")
    }

  private def getPages(mainPage: WSResponse, pagesCount: Int, cUrl: String, url: String): Future[Seq[WSResponse]] = {
    val pageCount = getPagesCount(mainPage)
    Future.sequence {
      (0 to (pageCount - pagesCount max 0)).map { i =>
        httpClient.url(s"$cUrl$url${40 * i}").get()
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