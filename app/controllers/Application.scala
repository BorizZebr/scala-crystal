package controllers

import javax.inject.Inject

import dao.{ChartsDAO, CompetitorsDAO, GoodsDAO, ReviewsDAO}
import models._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._

class Application @Inject()
(
  competitorsDAO: CompetitorsDAO,
  reviewsDAO: ReviewsDAO,
  goodsDAO: GoodsDAO,
  chartsDAO: ChartsDAO) extends Controller {

  implicit val jodaDateWrites = Writes.jodaDateWrites("yyyy-MM-dd")

  def index = Action {
    Ok(views.html.index("Ok, go!"))
  }

  def competitor = Action.async {
    implicit val competitorsWrite = Json.writes[Competitor]

    val competitors = competitorsDAO.getAll
    competitors.map{
      cs => Ok(Json.toJson(cs))
    }
  }

  def review(id: Long, skip: Int, take: Int) = Action.async {
    implicit val jodaDateWrites = Writes.jodaDateWrites("yyyy-MM-dd HH:mm")
    implicit val reviewWrite = Json.writes[Review]

    val reviews = reviewsDAO.getByCompetitor(id, skip, take)
    reviews.map {
      rv => Ok(Json.toJson(rv))
    }
  }

  def goods(id: Long, skip: Int, take: Int) = Action.async {
    implicit val goodWrite = Json.writes[Good]

    val goods = goodsDAO.getByCompetitor(id, skip, take)
    goods.map {
      gd => Ok(Json.toJson(gd))
    }
  }


  def chart(id: Long) = Action.async {
    implicit val chartPointWrite = Json.writes[ChartPoint]

    val points = chartsDAO.getPoints(id, 0, 30)
    points.map {
      pt => Ok(Json.toJson(pt))
    }
  }
}