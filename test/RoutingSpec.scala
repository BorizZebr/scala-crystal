import controllers._
import org.scalatestplus.play._
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
        FakeRequest(GET, routes.Application.competitor().url))

      status(competitors) mustBe OK
      contentType(competitors) mustBe Some("application/json")
    }

    "return charts JSON" in {
      val Some(charts) = route(app,
        FakeRequest(GET, routes.Application.chart(0).url))

      status(charts) mustBe OK
      contentType(charts) mustBe Some("application/json")
    }

    "return reviews JSON" in {
      val Some(reviews) = route(app,
        FakeRequest(GET, routes.Application.review(0, 0, 0).url))

      status(reviews) mustBe OK
      contentType(reviews) mustBe Some("application/json")
    }

    "return goods JSON" in {
      val Some(goods) = route(app,
        FakeRequest(GET, routes.Application.goods(0, 0, 0).url))

      status(goods) mustBe OK
      contentType(goods) mustBe Some("application/json")
    }
  }
}
