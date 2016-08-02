package scala

import controllers._
import models.{Pckg, PriceResponse}
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import play.api.mvc.Results
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.PriceCalculatorService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by borisbondarenko on 30.07.16.
  */
class PriceCalculatorControllerSpec extends PlaySpec
    with Results
    with MockitoSugar {

  val mockPricepriceCalcService = mock[PriceCalculatorService]
  var controller = new PriceCalculator(mockPricepriceCalcService)

  "PriceCalculator" when {
    "asked for price models" should {
      "return price models" in {
        // Arrange
        val pSeq = Seq("first", "second")
        when(mockPricepriceCalcService.getPriceModels) thenReturn Future(Option(pSeq))

        // Act
        val pricemodels = controller.priceModels apply FakeRequest()

        // Assert
        contentAsJson(pricemodels) mustEqual Json.toJson(pSeq)
      }
    }

    "asked for price" should {
      "return price in case of correct model" in {
        // Arrange
        val price = PriceResponse(10.0, 20.0, 30.0)
        when(mockPricepriceCalcService.getPrice("aaa", 10.0)) thenReturn Future(Option(price))

        // Act
        val priceResponse = controller.price("aaa", 10.0) apply FakeRequest()

        // Assert
        contentAsJson(priceResponse) mustEqual Json.toJson(price)
      }

      "return error in case of incorrect model" in {
        // Arrange
        when(mockPricepriceCalcService.getPrice(any[String], any[Double])) thenReturn Future(None)

        // Act
        val priceResponse = controller.price("aaa", 10.0) apply FakeRequest()

        // Assert
        status(priceResponse) mustEqual BAD_REQUEST
      }
    }

    "post new package" should {
      "successfully post" in {
        // Arrange
        when(mockPricepriceCalcService.storePackage(any[Pckg])) thenReturn Future((): Unit)

        // Act
        val response = controller.storePackage() apply FakeRequest(
          POST,
          controllers.routes.PriceCalculator.storePackage().url)
          .withJsonBody(Json.toJson(Pckg(10.0)))

        // Assert
        status(response) mustEqual OK
      }
    }
  }
}
