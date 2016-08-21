package scala.dal

import dal.components.{DalConfig, TypedComponent}
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

  val seq = Seq[TypedComponent](
    new CompetitorsRepo(this),
    new ReviewsRepo(this),
    new ChartsRepo(this),
    new GoodsRepo(this),
    new TestCrudRepo(this))

  override def beforeEach(): Unit = {
    val init = {
      val initSeq = DBIO.sequence(seq.map(_.table.schema.create))
      db.run(initSeq)
    }
    Await.result(init, Duration.Inf)
  }

  override protected def afterEach(): Unit = {
    val drop = {
      val dropSeq = DBIO.sequence(seq.map(_.table.schema.drop))
      db.run(dropSeq)
    }
    Await.result(drop, Duration.Inf)
  }
}

trait DalMatchers {
  def result[T](of: Future[T]): T = Await.result(of, Duration.Inf)
}
