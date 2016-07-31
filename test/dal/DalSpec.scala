package dal

import dal.components.DalConfig
import dal.repos.CompetitorsRepo
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}
import slick.driver.{H2Driver, JdbcProfile}
import slick.jdbc.JdbcBackend
import slick.jdbc.JdbcBackend.Database

/**
  * Created by borisbondarenko on 31.07.16.
  */
trait DalSpec extends Suite
    with BeforeAndAfterEach
    with BeforeAndAfterAll
    with DalConfig {

  val dbname = "scalaplayground_test"

  override val driver: JdbcProfile = H2Driver
  override val db: JdbcBackend#DatabaseDef = Database.forURL(
    s"jdbc:h2:mem:$dbname;DB_CLOSE_DELAY=-1",
    driver="org.h2.Driver")

  import driver.api._

  override def beforeAll(): Unit = {
    db.run(sql"""DROP DATABASE IF EXISTS $dbname""".as[String].transactionally)
    db.run(sql"""CREATE DATABASE $dbname""".as[String].transactionally)
  }

  override def afterAll(): Unit = {
    db.run(sql"""DROP DATABASE $dbname""".as[String].transactionally)
  }

  override def beforeEach(): Unit = {
    val competitorsRepo = new CompetitorsRepo(this)
    competitorsRepo.table.schema.create
  }

  override protected def afterEach(): Unit = {
    val competitorsRepo = new CompetitorsRepo(this)
    competitorsRepo.table.schema.drop
  }
}
