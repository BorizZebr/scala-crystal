package scala.controllers

import dal.repos.{ChartsDao, CompetitorsDao, GoodsDao, ReviewsDao}
import models._
import org.joda.time.LocalDate
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
class ApplicationControllerSpec extends PlaySpec
  with Results
  with MockitoSugar {

  "Application" should {

    import controllers._

    // Arrange
    val mockCompetitorsRepo = mock[CompetitorsDao]
    val mockReviewsRepo = mock[ReviewsDao]
    val mockGoodsRepo = mock[GoodsDao]
    val mockChartsRepo = mock[ChartsDao]

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
      import bootstrap.fakeinit.LoremIpsum

      // Arrange
      val rSeq = Seq(
        Review(Some(1), Some(1), LoremIpsum.words(2), LoremIpsum.paragraph),
        Review(Some(2), Some(1), LoremIpsum.words(2), LoremIpsum.paragraph),
        Review(Some(3), Some(1), LoremIpsum.words(2), LoremIpsum.paragraph))
      when(mockReviewsRepo.getByCompetitor(anyInt, anyInt, anyInt)) thenReturn Future(rSeq)

      // Act
      val reviews = controller.review(1, 100, 500) apply FakeRequest()

      // Assert
      contentAsJson(reviews) mustEqual Json.toJson(rSeq)
    }

    "return goods valid" in {
      import bootstrap.fakeinit.LoremIpsum

      // Arrange
      val gSeq = Seq(
        Good(Some(1), Some(1), 1, LoremIpsum.words(2), 123.321, LoremIpsum.word, LoremIpsum.word),
        Good(Some(2), Some(1), 2, LoremIpsum.words(2), 4342.323, LoremIpsum.word, LoremIpsum.word),
        Good(Some(3), Some(1), 3, LoremIpsum.words(2), 164.3431, LoremIpsum.word, LoremIpsum.word))
      when(mockGoodsRepo.getByCompetitor(anyInt, anyInt, anyInt)) thenReturn Future(gSeq)

      // Act
      val goods = controller.goods(1, 100, 500) apply FakeRequest()

      // Assert
      contentAsJson(goods) mustEqual Json.toJson(gSeq)
    }

    "return charts valid" in {
      // Arrange
      val chSeq = Seq(
        ChartPoint(LocalDate.now, 1, 2),
        ChartPoint(LocalDate.now, 3, 4),
        ChartPoint(LocalDate.now, 5, 6))
      when(mockChartsRepo.getPoints(anyInt, anyInt, anyInt)) thenReturn Future(chSeq)

      // Act
      val chart = controller.chart(1) apply FakeRequest()

      // Assert
      contentAsJson(chart) mustEqual Json.toJson(chSeq)
    }
  }
}
