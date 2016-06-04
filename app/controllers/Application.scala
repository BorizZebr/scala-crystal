package controllers

import javax.inject.Inject

import dal.repos.{ChartsRepo, GoodsRepo, ReviewsRepo, CompetitorsRepo}
import models.{ChartPoint, Good, Competitor, Review}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._

class Application @Inject()
(
  competitorsRepo: CompetitorsRepo,
  reviewsRepo: ReviewsRepo,
  goodsRepo: GoodsRepo,
  chartsRepo: ChartsRepo) extends Controller {

  def index = Action {
    Ok(views.html.index("Ok, go!"))
  }

  def competitor = Action.async {
    val competitors = competitorsRepo.getAll
    competitors.map{
      cs => Ok(Json.toJson(cs))
    }
  }

  def review(id: Long, skip: Int, take: Int) = Action.async {
    val reviews = reviewsRepo.getByCompetitor(id, skip, take)
    reviews.map {
      rv => Ok(Json.toJson(rv))
    }
  }

  def goods(id: Long, skip: Int, take: Int) = Action.async {
    val goods = goodsRepo.getByCompetitor(id, skip, take)
    goods.map {
      gd => Ok(Json.toJson(gd))
    }
  }

  def chart(id: Long) = Action.async {
    val points = chartsRepo.getPoints(id, 0, 30)
    points.map {
      pt => Ok(Json.toJson(pt))
    }
  }
}