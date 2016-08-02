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


}
