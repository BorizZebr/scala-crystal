package dal

import dal.repos.CompetitorsDao
import models.Competitor
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FunSpec, MustMatchers}

import scala.concurrent.Future

/**
  * Created by borisbondarenko on 26.05.16.
  */
class CompetitorsDaoSpec extends FunSpec
    with DalSpec
    with CompetitorsDao
    with DalMatchers
    with MustMatchers
    with ScalaFutures {

  import driver.api._

  describe("empty repo") {
    it("should be able to put new competitor") {
      // Act
      val res: Future[Unit] = insert(Competitor(None, "testtesttest", "url"))
      // Assert
      whenReady(res) { _ =>
        result(db.run(sql"""SELECT COUNT(*) FROM #$tableName""".as[Int])).head mustEqual 1
      }
    }
  }
}
