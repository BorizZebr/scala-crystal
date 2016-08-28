package scala.crawling

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestProbe
import com.zebrosoft.crystal.dal.repos.{ChartsDao, CompetitorsDao, GoodsDao, ReviewsDao}
import com.zebrosoft.crystal.model._
import crawling.PersisterActor._
import org.joda.time.LocalDate
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Matchers, WordSpecLike}

import scala.concurrent.Future

/**
  * Created by borisbondarenko on 06.08.16.
  */
class PersisterActorSpec(_system: ActorSystem) extends TestProbe(_system)
    with WordSpecLike
    with Matchers
    with MockitoSugar
    with BeforeAndAfterAll
    with BeforeAndAfterEach {

  import crawling.PersisterActor

  import scala.concurrent.ExecutionContext.Implicits.global

  val competitorsRepo = mock[CompetitorsDao]
  val chartsRepo = mock[ChartsDao]
  val reviewsRepo = mock[ReviewsDao]
  val goodsRepo = mock[GoodsDao]

  def this() = this(ActorSystem("PersisterActorSpec"))

  override def afterAll(): Unit = system.terminate()

  override def afterEach(): Unit = {
    reset(
      competitorsRepo,
      chartsRepo,
      reviewsRepo,
      goodsRepo)
  }

  "When updating competitor" should {

    val url = "url"
    val name = "name"

    "terminate itself after finishing work" in {
      // Arrange
      when(competitorsRepo.getByUrl(any[String])) thenReturn Future(None)
      val actor = system.actorOf(Props(classOf[PersisterActor],
        competitorsRepo,
        chartsRepo,
        reviewsRepo,
        goodsRepo))
      this watch actor

      // Act
      actor ! UpdateCompetitor(url, name)

      // Assert
      expectTerminated(actor)
    }

    "create new, when it is not exist" in {
      // Arrange
      when(competitorsRepo.getByUrl(url)) thenReturn Future(None)
      val actor = system.actorOf(Props(classOf[PersisterActor],
        competitorsRepo,
        chartsRepo,
        reviewsRepo,
        goodsRepo))

      // Act
      actor ! UpdateCompetitor(url, name)

      // Assert
      verify(competitorsRepo, timeout(500)).insert(Competitor(None, name, url, None, None, 0, 0))
    }

    "update existing in case it exists" in {
      // Arrange
      when(competitorsRepo.getByUrl(url)) thenReturn Future(Option(Competitor(Some(1), "azaza", url, None, None, 0, 0)))
      val actor = system.actorOf(Props(classOf[PersisterActor],
        competitorsRepo,
        chartsRepo,
        reviewsRepo,
        goodsRepo))

      // Act
      actor ! UpdateCompetitor(url, name)

      // Assert
      verify(competitorsRepo, timeout(500)).update(Competitor(Some(1), name, url, None, None, 0, 0))
    }

    "do nothing in case of updating existing competitor with same fields" in {
      // Arrange
      when(competitorsRepo.getByUrl(url)) thenReturn Future(Option(Competitor(Some(1), name, url, None, None, 0, 0)))
      val actor = system.actorOf(Props(classOf[PersisterActor],
        competitorsRepo,
        chartsRepo,
        reviewsRepo,
        goodsRepo))

      // Act
      actor ! UpdateCompetitor(url, name)

      // Assert
      verify(competitorsRepo, never()).update(any[Competitor])
      verify(competitorsRepo, never()).insert(any[Competitor])
    }
  }

  "When updating amount" should {

    val cmpId = 1
    val amount = 10
    val date = LocalDate.now

    "terminate itself after finishing work" in {
      // Arrange
      when(chartsRepo.getByCompetitorAndDate(any[Long], any[LocalDate])) thenReturn Future(None)
      val actor = system.actorOf(Props(classOf[PersisterActor],
        competitorsRepo,
        chartsRepo,
        reviewsRepo,
        goodsRepo))
      this watch actor

      // Act
      actor ! UpdateAmount(cmpId, amount, date)

      // Assert
      expectTerminated(actor)
    }

    "create new amount record in case it is not exists" in {
      // Arrange
      when(chartsRepo.getByCompetitorAndDate(cmpId, date)) thenReturn Future(None)
      val actor = system.actorOf(Props(classOf[PersisterActor],
        competitorsRepo,
        chartsRepo,
        reviewsRepo,
        goodsRepo))

      // Act
      actor ! UpdateAmount(cmpId, amount, date)

      // Assert
      verify(chartsRepo, timeout(500)).insert(Chart(None, Some(cmpId), amount, date))
    }

    "update amount record in case it is exists" in {
      // Arrange
      when(chartsRepo.getByCompetitorAndDate(cmpId, date)) thenReturn Future(Some(Chart(Some(1), Some(cmpId), 1, date)))
      val actor = system.actorOf(Props(classOf[PersisterActor],
        competitorsRepo,
        chartsRepo,
        reviewsRepo,
        goodsRepo))

      // Act
      actor ! UpdateAmount(cmpId, amount, date)

      // Assert
      verify(chartsRepo, timeout(500)).update(Chart(Some(1), Some(cmpId), amount, date))
    }
  }

  "When updating reviews" should {

    val reviews = Seq(
      Review(None, None, "author-1", "text-1", LocalDate.now),
      Review(None, None, "author-2", "text-2", LocalDate.now),
      Review(None, None, "author-3", "text-3", LocalDate.now))

    "terminate itself after finishing work" in {
      // Arrange
      val actor = system.actorOf(Props(classOf[PersisterActor],
        competitorsRepo,
        chartsRepo,
        reviewsRepo,
        goodsRepo))
      this watch actor

      // Act
      actor ! UpdateReviews(Nil)

      // Assert
      expectTerminated(actor)
    }

    "insert reviews" in {
      // Arrange
      when(reviewsRepo.contains(any[Review])) thenReturn Future(false)
      val actor = system.actorOf(Props(classOf[PersisterActor],
        competitorsRepo,
        chartsRepo,
        reviewsRepo,
        goodsRepo))

      // Act
      actor ! UpdateReviews(reviews)

      // Assert
      verify(reviewsRepo, timeout(500).times(3)).insert(any[Review])
    }
  }

  "When updating goods" should {

    val goods = Seq(
      Good(None, None, 1, "name-1", 10.0, "imgUrl-1", "url-1", LocalDate.now),
      Good(None, None, 2, "name-2", 20.0, "imgUrl-2", "url-2", LocalDate.now),
      Good(None, None, 3, "name-3", 30.0, "imgUrl-3", "url-3", LocalDate.now))

    "terminate itself after finishing work" in {
      // Arrange
      val actor = system.actorOf(Props(classOf[PersisterActor],
        competitorsRepo,
        chartsRepo,
        reviewsRepo,
        goodsRepo))
      this watch actor

      // Act
      actor ! UpdateGoods(Nil)

      // Assert
      expectTerminated(actor)
    }

    "insert reviews" in {
      // Arrange
      when(goodsRepo.contains(any[Good])) thenReturn Future(false)
      val actor = system.actorOf(Props(classOf[PersisterActor],
        competitorsRepo,
        chartsRepo,
        reviewsRepo,
        goodsRepo))

      // Act
      actor ! UpdateGoods(goods)

      // Assert
      verify(goodsRepo, timeout(500).times(3)).insert(any[Good])
    }
  }
}
