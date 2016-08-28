package services

import javax.inject.Inject

import akka.stream.Materializer
import com.zebrosoft.crystal.model.{Pckg, PriceResponse}
import play.api.Configuration
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.http.Status.OK
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.Future
import scala.util.Random

/**
  * Created by borisbondarenko on 28.07.16.
  */
trait PriceCalculatorService {

  def getPriceModels: Future[Option[Seq[String]]]

  def getPrice(model: String, weight: Double): Future[Option[PriceResponse]]

  def storePackage(pckg: Pckg): Future[Unit]
}

object FakePriceCalculator extends PriceCalculatorService {

  val modelNames = Set("test model - 1", "test model - 2")

  override def getPriceModels: Future[Option[Seq[String]]] = Future {
    Option(modelNames.toSeq)
  }

  override def getPrice(model: String, weight: Double): Future[Option[PriceResponse]] = Future {
    if (modelNames.contains(model)) {
      val rnd = new Random()
      Option(PriceResponse(weight, rnd.nextDouble() * weight, rnd.nextDouble() * weight))
    }
    else None
  }

  override def storePackage(pckg: Pckg): Future[Unit] = Future{}
}

class ProdPriceCalculator @Inject()(
    configuration: Configuration,
    implicit val mat: Materializer) extends PriceCalculatorService {

  val priceApiUrl: String = configuration.underlying.getString("api.priceCalculatorUrl")

  override def getPriceModels: Future[Option[Seq[String]]] = {
    val url = s"$priceApiUrl/models"

    AhcWSClient().url(url).get.map { r => r.status match {
        case OK => Option(r.json.as[Seq[String]])
        case _ => None
      }
    }
  }

  override def getPrice(model: String, weight: Double): Future[Option[PriceResponse]] = {
    implicit val priceInfoReads = Json.reads[PriceResponse]
    val url = s"$priceApiUrl/price/$model/$weight"

    AhcWSClient().url(url).get.map { r => r.status match {
        case OK => Option(r.json.as[PriceResponse])
        case _ => None
      }
    }
  }

  override def storePackage(pckg: Pckg): Future[Unit] = {
    implicit val packageWrite = Json.writes[Pckg]
    val url = s"$priceApiUrl/package"

    AhcWSClient().url(url).post(Json.toJson(pckg)).map(_ => ())
  }

}
