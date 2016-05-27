package dal.repos

import javax.inject.{Inject, Singleton}

import dal.components.{CompetitorsDependentComponent, CrudComponent, DatabaseComponent}
import models.Good
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

/**
  * Created by borisbondarenko on 27.05.16.
  */
@Singleton
class GoodsRepo @Inject() (val dbConfig: DatabaseConfig[JdbcProfile])
  extends DatabaseComponent
  with CrudComponent
  with CompetitorsDependentComponent {

  import driver.api._

  class GoodsTable(tag: Tag) extends Table[Good](tag, "GOOD")
    with IdColumn[Good]
    with CompetitorDependantColumns[Good] {

    def name = column[String]("NAME")
    def price = column[Double]("PRICE")
    def imgUrl = column[String]("IMG_URL")
    def url = column[String]("URL")
    override def * = (id.?, competitorId.?, name, price, imgUrl, url, date) <>(Good.tupled, Good.unapply)
  }

  override type Entity = Good
  override type EntityTable = GoodsTable
  override val table = TableQuery[GoodsTable]
}