import dal.repos.{ChartsRepo, GoodsRepo, ReviewsRepo, CompetitorsRepo}
import models.{ChartPoint, Good, Review}
import org.joda.time.DateTime
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play._
import play.api.libs.json.Json
import play.api.mvc.Results
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by borisbondarenko on 26.05.16.
  */
class ApplicationSpec extends PlaySpec
  with Results
  with MockitoSugar {

  "Application" should {

    import controllers._
    import models.Competitor

//    implicit val competitorWrite = Json.writes[Competitor]
//    implicit val reviewWrite = Json.writes[Review]
//    implicit val goodWrite = Json.writes[Good]
//    implicit val chartWrite = Json.writes[ChartPoint]

    // Arrange
    val mockCompetitorsRepo = mock[CompetitorsRepo]
    val mockReviewsRepo = mock[ReviewsRepo]
    val mockGoodsRepo = mock[GoodsRepo]
    val mockChartsRepo = mock[ChartsRepo]

    val controller = new Application(
      mockCompetitorsRepo,
      mockReviewsRepo,
      mockGoodsRepo,
      mockChartsRepo)

    "return competitors valid" in {
      // Arrange
      val cSeq = Seq(
        Competitor(Some(1), "AAA", "http://aaa.aaa"),
        Competitor(Some(2), "BBB", "http://bbb.bbb"),
        Competitor(Some(3), "CCC", "http://ccc.ccc"))
      when(mockCompetitorsRepo.getAll) thenReturn Future(cSeq)

      // Act
      val competitors = controller.competitor apply FakeRequest()

      // Assert
      contentAsJson(competitors) mustEqual Json.toJson(cSeq)
    }

    "return reviews valid" in {
      import bootstrap.init.LoremIpsum

      // Arrange
      val rSeq = Seq(
        Review(Some(1), Some(1), LoremIpsum.words(2), LoremIpsum.paragraph, DateTime.now),
        Review(Some(2), Some(1), LoremIpsum.words(2), LoremIpsum.paragraph, DateTime.now),
        Review(Some(3), Some(1), LoremIpsum.words(2), LoremIpsum.paragraph, DateTime.now))
      when(mockReviewsRepo.getByCompetitor(anyInt, anyInt, anyInt)) thenReturn Future(rSeq)

      // Act
      val reviews = controller.review(1, 100, 500) apply FakeRequest()

      // Assert
      contentAsJson(reviews) mustEqual Json.toJson(rSeq)
    }

    "return goods valid" in {
      import bootstrap.init.LoremIpsum

      // Arrange
      val gSeq = Seq(
        Good(Some(1), Some(1), LoremIpsum.words(2), 123.321, LoremIpsum.word, LoremIpsum.word, DateTime.now),
        Good(Some(2), Some(1), LoremIpsum.words(2), 4342.323, LoremIpsum.word, LoremIpsum.word, DateTime.now),
        Good(Some(3), Some(1), LoremIpsum.words(2), 164.3431, LoremIpsum.word, LoremIpsum.word, DateTime.now))
      when(mockGoodsRepo.getByCompetitor(anyInt, anyInt, anyInt)) thenReturn Future(gSeq)

      // Act
      val goods = controller.goods(1, 100, 500) apply FakeRequest()

      // Assert
      contentAsJson(goods) mustEqual Json.toJson(gSeq)
    }

    "return charts valid" in {

      // Arrange
      val chSeq = Seq(
        ChartPoint(DateTime.now, 1, 2),
        ChartPoint(DateTime.now, 3, 4),
        ChartPoint(DateTime.now, 5, 6))
      when(mockChartsRepo.getPoints(anyInt, anyInt, anyInt)) thenReturn Future(chSeq)

      // Act
      val chart = controller.chart(1) apply FakeRequest()

      // Assert
      contentAsJson(chart) mustEqual Json.toJson(chSeq)
    }
  }
}
