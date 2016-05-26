package dal

import models.Good

/**
  * Created by borisbondarenko on 27.05.16.
  */
trait GoodsComponent extends DatabaseComponent
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