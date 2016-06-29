package dal.repos

import javax.inject.{Inject, Singleton}

import dal.components.{CompetitorsDependentComponent, CrudComponent, DalConfig, DatabaseComponent}
import models.Good

import scala.concurrent.Future

/**
  * Created by borisbondarenko on 27.05.16.
  */
@Singleton
class GoodsRepo @Inject() (val dalConfig: DalConfig)
  extends DatabaseComponent
  with CrudComponent
  with CompetitorsDependentComponent {

  import driver.api._

  class GoodsTable(tag: Tag) extends Table[Good](tag, "GOOD")
    with IdColumn[Good]
    with CompetitorDependantColumns[Good] {

    def extId = column[Long]("EXT_ID")
    def name = column[String]("NAME")
    def price = column[Double]("PRICE")
    def imgUrl = column[String]("IMG_URL")
    def url = column[String]("URL")
    override def * = (id.?, competitorId.?, extId, name, price, imgUrl, url, date) <>(Good.tupled, Good.unapply)
  }

  override type Entity = Good
  override type EntityTable = GoodsTable
  override val table = TableQuery[GoodsTable]

  override def getByCompetitor(competitorId: Long, skip: Int, take: Int): Future[Seq[Good]] =
    db.run(table
      .filter(_.competitorId === competitorId)
      .sortBy(_.extId.desc)
      .drop(skip)
      .take(take)
      .result)

  override def contains(entity: Good): Future[Boolean] =
    db.run {
      table.filter { en =>
        en.competitorId === entity.competitorId &&
        en.extId === entity.extId
      }.result.headOption
    }.map(_.isEmpty)
}