package dal

import dal.repos.CompetitorsDao
import models.Competitor
import org.scalatest.FunSpec

/**
  * Created by borisbondarenko on 26.05.16.
  */
class CompetitorsDaoSpec extends FunSpec
    with DalSpec
    with CompetitorsDao {

  describe("empty repo") {
    it("should be able to put new competitor") {
      insert(Competitor(None, "testtesttest", "url"))
    }
  }
}
