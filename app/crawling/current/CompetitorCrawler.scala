package crawling.current

import java.nio.charset.StandardCharsets

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.zebrosoft.crystal.dal.repos.{ChartsDao, CompetitorsDao, GoodsDao, ReviewsDao}
import com.zebrosoft.crystal.model.{Chart, Competitor, Good, Review}
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.joda.time.{DateTime, LocalDate}
import org.jsoup.Jsoup
import play.api.Logger
import play.api.libs.ws.WSResponse
import play.api.libs.ws.ahc.AhcWSClient

import scala.collection.JavaConversions._
import scala.concurrent.Future

/**
  * Created by borisbondarenko on 26.09.16.
  */
class CompetitorCrawler (
    cmp: Competitor,
    competitorsRepo: CompetitorsDao,
    chartsRepo: ChartsDao,
    reviewsRepo: ReviewsDao,
    goodsRepo: GoodsDao
) {

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit class WSResponseImprovement(val response: WSResponse) {
    def bodyAsUTF8: String = new String(response.bodyAsBytes.toArray, StandardCharsets.UTF_8)
  }

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val client = AhcWSClient()

  val formatter: DateTimeFormatter = DateTimeFormat.forPattern("dd.MM.yyyy")

  def crawlCompetitor() = {

    val startTime = DateTime.now
    competitorsRepo.update(cmp.copy(
      lastCrawlStart = Some(startTime),
      lastCrawlFinish = None))
    val mainFuture = getPage(s"${cmp.url}/?sortitems=4&v=0")
    val firstReviewsFuture = getPage(s"${cmp.url}/feedbacks")

    val chain = for {
      main <- mainFuture
      firstReviews <- firstReviewsFuture

      goodsPages <- getPages(main, cmp.crawledGoodsPages, 40, cmp.url, "?sortitems=0&v=0&from=")
      _ = Logger.info(s"${cmp.name} Goods Pages Count -- ${goodsPages.size}")

      reviewsPages <- getPages(firstReviews, cmp.crawledReviewsPages, 20, cmp.url, "/feedbacks?status=m&from=")
      _ = Logger.info(s"${cmp.name} Reviews Pages Count -- ${reviewsPages.size}")

      goods <- parseGoods(goodsPages)
      _ = Logger.info(s"${cmp.name} Goods Count -- ${goods.size}")

      reviews <- parseReviews(reviewsPages)
      _ = Logger.info(s"${cmp.name} Reviews Count -- ${reviews.size}")

      _ <- updateReviews(reviews)
      _ <- updateGoods(goods)

      subscribersAmount = Jsoup.parse(main.bodyAsUTF8).select("#totalSubscribers").text.toInt
      _ <- updateAmount(subscribersAmount, LocalDate.now)
    } yield (goodsPages.size, reviewsPages.size)

    chain.onComplete(_ => client.close())

    chain.map { case(gs, rs) =>
      val updatedCmp = cmp.copy(
        lastCrawlStart = Some(startTime),
        lastCrawlFinish = Some(DateTime.now),
        crawledGoodsPages = cmp.crawledGoodsPages + gs,
        crawledReviewsPages = cmp.crawledReviewsPages + rs)
      competitorsRepo.update(updatedCmp)
    }
  }

  def parseReviews(reviewsPages: Seq[WSResponse]): Future[Seq[Review]] =
    Future.sequence(reviewsPages map parseReviewsPage).map(_.flatten)

  def parseGoods(goodsPages: Seq[WSResponse]): Future[Seq[Good]] =
    Future.sequence(goodsPages map parseGoodsPage).map(_.flatten)

  def parseReviewsPage(page: WSResponse): Future[Seq[Review]] = {
    Future {
      val r = Jsoup.parse(page.bodyAsUTF8)
      (
        r.select("div.grid-content.wordwrap.text-desc > a").map(_.text),
        r.select("div.grid-550.list-item-content > div.grid-auto.gray[title]").map(_.text),
        r.select("div.grid-530").map(_.text)).zipped.toSeq
        .map { el => Review(None, cmp.id, el._1, el._3, LocalDate.parse(el._2, formatter)) }
    }
  }

  def parseGoodsPage(page: WSResponse): Future[Seq[Good]] = {
    Future {
      val g = Jsoup.parse(page.bodyAsUTF8)
      for (el <- g.select("div.b-item.b-item-hover")) yield {
        val id = el.attr("id").replaceAll("[^\\d.]", "").toLong // id
        val name = el.select("div.title > a").text // name
        val price = el.select(".price").text.replaceAll("[^\\d.]", "") // price
        val imgUrl = el.select("img").attr("src") // img url
        val itemUrl = "https://www.livemaster.ru/" + el.select("a").attr("href") // item url

        Good(
          None,
          cmp.id,
          id, name,
          if (price.isEmpty) 0 else price.toDouble,
          imgUrl,
          itemUrl,
          LocalDate.now)
      }
    }
  }

  def updateAmount(am: Int, date: LocalDate): Future[Unit] = {
    chartsRepo
      .getByCompetitorAndDate(cmp.id.get, date)
      .flatMap {
        case Some(x) =>
          val chart = x.updateAmount(am)
          chartsRepo.update(chart)

        case None =>
          val chart = Chart(None, cmp.id, am, LocalDate.now)
          chartsRepo.insert(chart).map(_ => ())
      }
  }

  def updateReviews(reviews: Seq[Review]) = {
    val futures = reviews.map { r =>
      reviewsRepo.contains(r).flatMap { c =>
        if(c) Future.successful(None)
        else reviewsRepo.insert(r) map Option.apply
      }
    }

    val res = Future.sequence(futures)
    res.onFailure {
      case e:Exception => Logger.error(s"ALARM REVIEWS -- ${e.getMessage}")
    }
    res
  }


  def updateGoods(goods: Seq[Good]) = {
    val futures = goods.map { g =>
      goodsRepo.contains(g).flatMap { c =>
        if(c) Future.successful(None)
        else goodsRepo.insert(g) map Option.apply
      }
    }

    val res = Future.sequence(futures)
    res.onFailure {
      case e:Exception => Logger.error(s"ALARM GOODS -- ${e.getMessage}")
    }
    res

  }

  def getPage(url: String): Future[WSResponse] =
    client.url(url).get().map {
      case res if res.status != 200 =>
        Logger.error(s"$url status ${res.status}")
        throw new Exception()
      case res =>
        Logger.info(s"$url status OK")
        res
    }

  def getPages(
      mainPage: WSResponse,
      pagesCount: Int,
      perPage: Int,
      cUrl: String,
      url: String): Future[Seq[WSResponse]] = {
    val pageCount = getPagesCount(mainPage)
    Future.sequence {
      (0 to (pageCount - pagesCount max 0)).map { i =>
        Thread.sleep(250)
        getPage(s"$cUrl$url${perPage * i}")
      }
    }
  }

  def getPagesCount(resp: WSResponse): Int =
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
