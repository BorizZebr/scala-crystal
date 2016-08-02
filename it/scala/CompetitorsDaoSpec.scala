package dal

import dal.repos.CompetitorsDao
import models.Competitor
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterEach, FunSpec, MustMatchers}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by borisbondarenko on 26.05.16.
  */
class CompetitorsDaoSpec extends FunSpec
    with DalSpec
    with CompetitorsDao
    with DalMatchers
    with MustMatchers
    with ScalaFutures
    with BeforeAndAfterEach {

  import driver.api._

  val testCompetitors = Map[Int, Competitor](
    1 -> Competitor(Option(1), "Test-1", "Url-1"),
    2 -> Competitor(Option(2), "Test-2", "Url-2"),
    3 -> Competitor(Option(3), "Test-3", "Url-3"))

  override def beforeEach(): Unit = {
    super.beforeEach()
    Await.result(insert(testCompetitors.values.toSeq), Duration.Inf)
  }

  it("should get competitor by url") {
    val competitor = testCompetitors(1)
    result(getByUrl(competitor.url)).get.name mustEqual competitor.name
  }

  it("should return None competitor by incorrect url") {
    result(getByUrl("azaza")) mustEqual None
  }

  it("should assume true on containing element by correct name + url") {
    val competitorToContain = testCompetitors(1)
    result(contains(competitorToContain)) mustEqual true
  }

  it("should assume false on containing element by incorrect name") {
    val competitorToContain = testCompetitors(1).copy(name = "azaza")
    result(contains(competitorToContain)) mustEqual false
  }

  it("should assume false on containing element incorrect url") {
    val competitorToContain = testCompetitors(1).copy(url = "azaza")
    result(contains(competitorToContain)) mustEqual false
  }

  it("should assume false on containing element by incorrect name + url") {
    val competitorToContain = testCompetitors(1).copy(name = "azaza", url = "azaza")
    result(contains(competitorToContain)) mustEqual false
  }
}
