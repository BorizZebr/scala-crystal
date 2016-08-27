package scala.routing

import controllers._
import models.{Pckg, ResponseTemplate}
import org.scalatestplus.play._
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test._

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
      contentAsString(home) must {
        include ("Очень злая система") and
        include ("Корпорация Зла")
      }
    }

    "render templates page" in {
      val Some(tmplts) = route(app, FakeRequest(GET, "/templates"))

      status(tmplts) mustBe OK
      contentType(tmplts) mustBe Some("text/html")
      contentAsString(tmplts) must {
        include ("Дубовики") and
        include ("Добавить")
      }
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

    "store new package POST" in {
      val Some(pckg) = route(app,
        FakeRequest(
          POST,
          "/storePackage",
          FakeHeaders(),
          Json.toJson(Pckg(100.0))))

      status(pckg) mustBe OK
      contentType(pckg) mustBe None
    }

    "return response templates JSON" in {
      val Some(tmplts) = route(app,
        FakeRequest(GET, "/responsetemplates"))

      status(tmplts) mustBe OK
      contentType(tmplts) mustBe Some("application/json")
    }

    "store new template PUT" in {
      val Some(tmplt) = route(app,
        FakeRequest(
          POST,
          "/responsetemplates",
          FakeHeaders(),
          Json.toJson(ResponseTemplate(
            Some(100500),
            "name",
            "text"))))

      status(tmplt) mustBe OK
      contentType(tmplt) mustBe Some("application/json")
    }

    "update template POST" in {
      val Some(tmplt) = route(app,
        FakeRequest(
          PUT,
          "/responsetemplates",
          FakeHeaders(),
          Json.toJson(ResponseTemplate(
            None,
            "name",
            "text"))))

      status(tmplt) mustBe OK
      contentType(tmplt) mustBe None
    }

    "delete template DELETE" in {
      val Some(tmplt) = route(app,
        FakeRequest(
          DELETE,
          "/responsetemplates/100500"))

      status(tmplt) mustBe OK
      contentType(tmplt) mustBe None
    }
  }
}
