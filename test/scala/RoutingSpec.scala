package scala

import controllers._
import models.Pckg
import org.scalatestplus.play._
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class RoutingSpec extends PlaySpec
  with OneAppPerSuite {

  "Application routing" should {

    "send 404 on a bad request" in {
      val Some(wrongRoute) = route(app, FakeRequest(GET, "/boum"))

      status(wrongRoute) mustBe NOT_FOUND
    }

    "render the index page" in {
      val Some(home) = route(app, FakeRequest(GET, "/"))

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include ("Очень злая система")
      contentAsString(home) must include ("Корпорация Зла")
    }

    "return competitors JSON" in {
      val Some(competitors) = route(app,
        FakeRequest(GET, "/competitors"))

      status(competitors) mustBe OK
      contentType(competitors) mustBe Some("application/json")
    }

    "return charts JSON" in {
      val Some(charts) = route(app,
        FakeRequest(GET, "/chart/0"))

      status(charts) mustBe OK
      contentType(charts) mustBe Some("application/json")
    }

    "return reviews JSON" in {
      val Some(reviews) = route(app,
        FakeRequest(GET, "/review/0"))

      status(reviews) mustBe OK
      contentType(reviews) mustBe Some("application/json")
    }

    "return goods JSON" in {
      val Some(goods) = route(app,
        FakeRequest(GET, "/goods/0"))

      status(goods) mustBe OK
      contentType(goods) mustBe Some("application/json")
    }

    "return price models JSON" in {
      val Some(priceModels) = route(app,
        FakeRequest(GET, "/pricemodels"))

      status(priceModels) mustBe OK
      contentType(priceModels) mustBe Some("application/json")
    }

    "return price JSON" in {
      val Some(price) = route(app,
        FakeRequest(GET, "/price/test%20model%20-%201/0.0"))

      status(price) mustBe OK
      contentType(price) mustBe Some("application/json")
    }

    "return BAD_REQUEST on not existing model" in {
      val Some(price) = route(app,
        FakeRequest(GET, "/price/fake model/0.0"))

      status(price) mustBe BAD_REQUEST
    }

    "store new package" in {
      val Some(pckg) = route(app,
        FakeRequest(
          POST,
          "/storePackage",
          FakeHeaders(),
          Json.toJson(Pckg(100.0))))

      status(pckg) mustBe OK
      contentType(pckg) mustBe None
    }
  }
}
