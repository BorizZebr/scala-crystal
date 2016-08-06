package dal

import dal.components.DalConfig
import dal.repos.{ChartsRepo, CompetitorsRepo, GoodsRepo, ReviewsRepo}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}
import slick.driver.{H2Driver, JdbcProfile}
import slick.jdbc.JdbcBackend
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created by borisbondarenko on 31.07.16.
  */
trait DalSpec extends Suite
    with BeforeAndAfterEach
    with BeforeAndAfterAll
    with DalConfig {

  override val driver: JdbcProfile = H2Driver
  override val db: JdbcBackend#DatabaseDef = Database.forConfig("h2test")

  import driver.api._

  override def beforeEach(): Unit = {
    val competitorsRepo = new CompetitorsRepo(this)
    val reviewsRepo = new ReviewsRepo(this)
    val chartsRepo = new ChartsRepo(this)
    val goodsRepo = new GoodsRepo(this)
    val testRepo = new TestCrudRepo(this)

    val init = {
      val initSeq = DBIO.seq(
        competitorsRepo.table.schema.create,
        reviewsRepo.table.schema.create,
        chartsRepo.table.schema.create,
        goodsRepo.table.schema.create,
        testRepo.table.schema.create)
      db.run(initSeq)
    }
    Await.result(init, Duration.Inf)
  }

  override protected def afterEach(): Unit = {
    val competitorsRepo = new CompetitorsRepo(this)
    val reviewsRepo = new ReviewsRepo(this)
    val chartsRepo = new ChartsRepo(this)
    val goodsRepo = new GoodsRepo(this)
    val testRepo = new TestCrudRepo(this)

    val drop = {
      val dropSeq = DBIO.seq(
        competitorsRepo.table.schema.drop,
        reviewsRepo.table.schema.drop,
        chartsRepo.table.schema.drop,
        goodsRepo.table.schema.drop,
        testRepo.table.schema.drop)
      db.run(dropSeq)
    }
    Await.result(drop, Duration.Inf)
  }
}

trait DalMatchers {
  def result[T](of: Future[T]): T = Await.result(of, Duration.Inf)
}
