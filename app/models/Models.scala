package models

import org.joda.time.{DateTime, LocalDate}
import play.api.libs.json.{Json, Writes}

/**
  * Created by borisbondarenko on 25.05.16.
  */
case class Competitor(
    id: Option[Long],
    name: String,
    url: String,
    lastCrawlStart: Option[DateTime] = None,
    lastCrawlFinish: Option[DateTime] = None,
    crawledGoodsPages: Int = 0,
    crawledReviewsPages: Int = 0) {

  def updateNameAndUrl(name: String, url: String) = copy(
    this.id,
    name,
    url,
    this.lastCrawlStart,
    this.lastCrawlFinish,
    this.crawledGoodsPages,
    this.crawledReviewsPages
  )
}

case class Good(
  id: Option[Long],
  competitorId: Option[Long],
  extId: Long, name: String,
  price: Double,
  imgUrl: String,
  url: String,
  date: LocalDate = LocalDate.now)

case class Review(
  id: Option[Long],
  competitorId: Option[Long],
  author: String,
  text: String,
  date: LocalDate = LocalDate.now)

case class Chart(
    id: Option[Long],
    competitorId: Option[Long],
    amount: Int,
    date: LocalDate) {
  def updateAmount(amount: Int) = copy(
    this.id, this.competitorId, amount, this.date)
}

case class ChartPoint(x: LocalDate, amount: Int, change: Int)

case class Pckg(weight: Double)

case class PriceResponse(weight: Double, priceByCalc: Double, priceByPost: Double)