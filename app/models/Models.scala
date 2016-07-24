package models

import org.joda.time.{DateTime, LocalDate}

/**
  * Created by borisbondarenko on 25.05.16.
  */
case class Competitor
(
    id: Option[Long],
    name: String,
    url: String,
    lastCrawlStart: Option[DateTime],
    lastCrawlFinish: Option[DateTime],
    crawledGoodsPages: Int,
    crawledReviewsPages: Int) {

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

case class Good(id: Option[Long], competitorId: Option[Long], extId: Long, name: String, price: Double, imgUrl: String, url: String, date: LocalDate)

case class Review(id: Option[Long], competitorId: Option[Long], author: String, text: String, date: LocalDate)

case class Chart(id: Option[Long], competitorId: Option[Long], amount: Int, date: LocalDate) {
  def updateAmount(amount: Int) = copy(
    this.id, this.competitorId, amount, this.date)
}

case class ChartPoint(x: LocalDate, amount: Int, change: Int)

case class Pckg(weight: Double)