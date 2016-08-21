package scala.dal

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FunSpec, MustMatchers}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by borisbondarenko on 02.08.16.
  */
class CrudComponentSpec extends FunSpec
  with DalSpec
  with TestCrudDao
  with DalMatchers
  with MustMatchers
  with ScalaFutures {

  import driver.api._

  describe("empty table") {
    it("contains zero records") {
      result(count) mustEqual 0
    }

    it("should be able to put new entity") {
      whenReady(insert(TestEntity(None, "testtesttest"))) { _ =>
        result(db.run(sql"""SELECT COUNT(*) FROM #$tableName""".as[Int])).head mustEqual 1
      }
    }

    it("should be able to put collection of new entities") {
      val seq = Seq(
        TestEntity(None, "test-1"),
        TestEntity(None, "test-2"),
        TestEntity(None, "test-3"))
      whenReady(insert(seq)) { _ =>
        result(db.run(sql"""SELECT COUNT(*) FROM #$tableName""".as[Int])).head mustEqual 3
      }
    }
  }

  describe("not empty table") {

    val testSeq = Map[Int, TestEntity](
      1 -> TestEntity(Option(1), "Test-1"),
      2 -> TestEntity(Option(2), "Test-2"),
      3 -> TestEntity(Option(3), "Test-3"))

    def init() = {
      val initSeq = DBIO.sequence(
        testSeq.values.map { el =>
          sqlu"insert into #$tableName values(#${el.id.get}, '#${el.name}')"
        })
      Await.result(db.run(initSeq), Duration.Inf)
    }

    it("should return correct count") {
      init()
      result(count) mustEqual 3
    }

    it("should return all elements") {
      init()
      result(getAll) mustEqual testSeq.values.toSeq
    }

    it("should return by id") {
      init()
      result(getById(1)).get mustEqual testSeq(1)
    }

    it("should update entity") {
      val updatedName = "UPDATED"
      init()
      whenReady(update(TestEntity(Option(1), updatedName))) { _ =>
        result(db.run(sql"""SELECT NAME FROM #$tableName WHERE id = 1""".as[String])).head mustEqual updatedName
      }
    }
  }
}
