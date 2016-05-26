package controllers

import javax.inject.Inject

import dal.dao.{ReviewsDAO, GoodsDAO, CompetitorsDAO, ChartsDAO}
import dal.ReviewsDAO
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
    val competitors = competitorsDAO.getAll
    competitors.map{
      cs => Ok(Json.toJson(cs))
    }
  }

  def review(id: Long, skip: Int, take: Int) = Action.async {
    implicit val jodaDateWrites = Writes.jodaDateWrites("yyyy-MM-dd HH:mm")

    val reviews = reviewsDAO.getByCompetitor(id, skip, take)
    reviews.map {
      rv => Ok(Json.toJson(rv))
    }
  }

  def goods(id: Long, skip: Int, take: Int) = Action.async {
    val goods = goodsDAO.getByCompetitor(id, skip, take)
    goods.map {
      gd => Ok(Json.toJson(gd))
    }
  }


  def chart(id: Long) = Action.async {
    val points = chartsDAO.getPoints(id, 0, 30)
    points.map {
      pt => Ok(Json.toJson(pt))
    }
  }
}