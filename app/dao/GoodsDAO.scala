package dao

import javax.inject.{ Inject, Singleton }

import models.Good
import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.driver.JdbcProfile

/**
  * Created by borisbondarenko on 25.05.16.
  */
@Singleton()
class GoodsDAO @Inject()
(
  protected val dbConfigProvider: DatabaseConfigProvider,
  protected val competitorsDAO: CompetitorsDAO)
  extends HasDatabaseConfigProvider[JdbcProfile]
    with CrudDAO
    with CompetitorDependentDAO {

  import driver.api._

  class GoodsTable(tag: Tag) extends Table[Good](tag, "GOOD")
    with IdColumn[Good]
    with CompetitorDependantColumns[Good] {

    def competitor = foreignKey("DIR_FK", competitorId, competitorsDAO.table)(_.id)

    def name = column[String]("NAME")
    def price = column[Double]("PRICE")
    def imgUrl = column[String]("IMG_URL")
    def url = column[String]("URL")
    override def * = (id.?, competitorId.?, name, price, imgUrl, url, date) <> (Good.tupled, Good.unapply)
  }

  override type Entity = Good
  override type EntityTable = GoodsTable
  override val table = TableQuery[GoodsTable]
}