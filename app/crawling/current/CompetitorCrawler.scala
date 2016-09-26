package crawling.current

import java.nio.charset.StandardCharsets

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.zebrosoft.crystal.dal.repos.{ChartsDao, GoodsDao, ReviewsDao}
import com.zebrosoft.crystal.model.{Chart, Competitor, Good, Review}
import org.joda.time.LocalDate
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
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

    val mainFuture = getPage(s"${cmp.url}/?sortitems=4&v=0", "main page")
    val firstReviewsFuture = getPage(s"${cmp.url}/feedbacks", "main reviews")

    for {
      main <- mainFuture
      firstReviews <- firstReviewsFuture
      reviewsPages <- getPages(firstReviews, cmp.crawledReviewsPages, 20, cmp.url, "/feedbacks?status=m&from=")
      goodsPages <- getPages(main, cmp.crawledGoodsPages, 40, cmp.url, "?sortitems=0&v=0&from=")
    } {

      Logger.info(s"Goods Pages Count -- ${goodsPages.size}")
      Logger.info(s"Reviews Pages Count -- ${reviewsPages.size}")

      val subscribersAmount = Jsoup.parse(main.bodyAsUTF8).select("#totalSubscribers").text.toInt
      updateAmount(subscribersAmount, LocalDate.now)

      val parsedGoods = Future.sequence(goodsPages map parseGoodsPage).map(_.flatten)
      val parsedReviews = Future.sequence(reviewsPages map parseReviewsPage).map(_.flatten)

      for {
        goods <- parsedGoods
        reviews <- parsedReviews
      } yield {
        Logger.info(s"Goods Count -- ${goods.size}")
        Logger.info(s"Reviews Count -- ${reviews.size}")

        updateGoods(goods)
        updateReviews(reviews)
      }
    }
  }

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

  def parseGoodsPage(page:WSResponse): Future[Seq[Good]] = {
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

  def updateAmount(am: Int, date: LocalDate) = {
    chartsRepo
      .getByCompetitorAndDate(cmp.id.get, date)
      .map {
        case Some(x) =>
          val chart = x.updateAmount(am)
          chartsRepo.update(chart)

        case None =>
          val chart = Chart(None, cmp.id, am, LocalDate.now)
          chartsRepo.insert(chart)
      }
  }

  def updateReviews(reviews: Seq[Review]) =
    for {
      r <- reviews
      c <- reviewsRepo.contains(r)
      if !c
    } reviewsRepo.insert(r)

  def updateGoods(goods: Seq[Good]) =
    for {
      g <- goods
      c <- goodsRepo.contains(g)
      if !c
    } goodsRepo.insert(g)

  def getPage(url: String, log: String): Future[WSResponse] = {
    val req = client.url(url).get()
    req.map { res =>
      Logger.info(s"$log status ${res.status}")
      res
    }
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
        Thread.sleep(500)
        getPage(s"$cUrl$url${perPage * i}", s"crawl $url page $i")
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
