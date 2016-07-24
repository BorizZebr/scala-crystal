package controllers

import javax.inject.Inject

import dal.repos.{ChartsRepo, CompetitorsRepo, GoodsRepo, ReviewsRepo}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._

class Application @Inject()
(
  competitorsRepo: CompetitorsRepo,
  reviewsRepo: ReviewsRepo,
  goodsRepo: GoodsRepo,
  chartsRepo: ChartsRepo) extends Controller {

  /**
    * Index page controller
    * @return
    */
  def index = Action {
    Ok(views.html.index("Ok, go!"))
  }

  /**
    * Logs page controller
    * @return
    */
  def logs = Action {
    Ok(views.html.logs())
  }

  /**
    * GET Competitors API
    * @return All competitors registered in system
    */
  def competitor = Action.async {
    val competitors = competitorsRepo.getAll
    competitors.map{
      cs => Ok(Json.toJson(cs))
    }
  }

  /**
    * GET Reviews API
    * @param id Id of competitor
    * @param skip Reviews to skip
    * @param take Reviews to take
    * @return Reviews for competitor
    */
  def review(id: Long, skip: Int, take: Int) = Action.async {
    val reviews = reviewsRepo.getByCompetitor(id, skip, take)
    reviews.map {
      rv => Ok(Json.toJson(rv))
    }
  }

  /**
    * GET Goods API
    * @param id Id of competitor
    * @param skip Goods to skip
    * @param take Goods to take
    * @return Goods for competitor
    */
  def goods(id: Long, skip: Int, take: Int) = Action.async {
    val goods = goodsRepo.getByCompetitor(id, skip, take)
    goods.map {
      gd => Ok(Json.toJson(gd))
    }
  }

  /**
    * GET Chart points API
    * @param id Id of competitor
    * @return Chart for passed 30 days
    */
  def chart(id: Long) = Action.async {
    val points = chartsRepo.getPoints(id, 0, 30)
    points.map {
      pt => Ok(Json.toJson(pt))
    }
  }
}