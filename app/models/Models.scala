package models

import org.joda.time.DateTime

/**
  * Created by borisbondarenko on 25.05.16.
  */
case class Competitor(id: Option[Long], name: String, url: String, lastCrawlStart: Option[DateTime], lastCrawlFinish: Option[DateTime])

case class Good(id: Option[Long], competitorId: Option[Long], name: String, price: Double, imgUrl: String, url: String, date: DateTime)

case class Review(id: Option[Long], competitorId: Option[Long], author: String, text: String, date: DateTime)

case class Chart(id: Option[Long], competitorId: Option[Long], amount: Int, date: DateTime)

case class ChartPoint(x: DateTime, amount: Int, change: Int)