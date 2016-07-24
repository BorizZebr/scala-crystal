package controllers

import javax.inject.Inject

import akka.stream.Materializer
import models.Pckg
import play.api.Configuration
import play.api.libs.ws.ahc.AhcWSClient
import play.api.mvc.{Action, Controller}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json


/**
  * Created by borisbondarenko on 25.07.16.
  */
class PriceCalculator @Inject()(
    configuration: Configuration,
    implicit val mat: Materializer) extends Controller {

  val priceApiUrl = configuration.underlying.getString("api.priceCalculatorUrl")

  def priceModels = Action.async {
    val httpClient: AhcWSClient = AhcWSClient()
    val url = s"$priceApiUrl/models"

    httpClient.url(url).get().map {
      response => Ok(response.json)
    }
  }

  def getPrice(model: String, weight: Double) = Action.async {
    val httpClient: AhcWSClient = AhcWSClient()
    val url = s"$priceApiUrl/prices/$model/$weight"

    httpClient.url(url).get.map {
      response => Ok(response.json)
    }
  }

  def storePackage = Action.async { request =>
    val json = request.body.asJson.get
    val pckg = json.as[Pckg]

    val httpClient: AhcWSClient = AhcWSClient()
    val url = s"$priceApiUrl/package"

    httpClient.url(url).post(Json.toJson(pckg)).map {
      r => Ok(r.body)
    }
  }
}
