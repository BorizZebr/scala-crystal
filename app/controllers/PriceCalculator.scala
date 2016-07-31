package controllers

import javax.inject.Inject

import models.Pckg
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services.PriceCalculatorService

/**
  * Created by borisbondarenko on 25.07.16.
  */
class PriceCalculator @Inject()(
    priceCalcService: PriceCalculatorService) extends Controller {

  def priceModels = Action.async {
    priceCalcService.getPriceModels.map {
      case Some(x) => Ok(Json.toJson(x))
      case None => BadRequest("Something bad happened...")
    }
  }


  def price(model: String, weight: Double) = Action.async {
    priceCalcService.getPrice(model, weight).map {
      case Some(x) => Ok(Json.toJson(x))
      case None => BadRequest("No such model!")
    }
  }

  def storePackage = Action.async { request =>
    val json = request.body.asJson.get
    val pckg = json.as[Pckg]

    priceCalcService.storePackage(pckg).map { _ =>
      Ok
    }
  }
}
