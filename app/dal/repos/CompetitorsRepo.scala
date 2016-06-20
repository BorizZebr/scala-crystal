package dal
package repos

import javax.inject.{Inject, Singleton}

import dal.components.{CrudComponent, DalConfig, DatabaseComponent}
import models.Competitor
import org.joda.time.DateTime

import scala.concurrent.Future

/**
  * Created by borisbondarenko on 26.05.16.
  */
@Singleton()
class CompetitorsRepo @Inject() (val dalConfig: DalConfig)
  extends DatabaseComponent
  with CrudComponent {

  import driver.api._

  class CompetitorsTable(tag: Tag) extends Table[Competitor](tag, "COMPETITOR")
    with IdColumn[Competitor] {

    def name = column[String]("NAME")
    def url = column[String]("URL")
    def lastCrawlStart = column[Option[DateTime]]("LAST_CRAWL_START")
    def lastCrawlFinish = column[Option[DateTime]]("LAST_CRAWL_FINISH")
    def crawledGoodsPages = column[Option[DateTime]]("CRAWLED_GOODS_PAGES")
    def crawledReviewsPages = column[Option[DateTime]]("CRAWLED_REVIEWS_PAGES")
    override def * = (id.?, name, url, lastCrawlStart, lastCrawlFinish, crawledGoodsPages, crawledReviewsPages) <>
      (Competitor.tupled, Competitor.unapply)
  }

  override type Entity = Competitor
  override type EntityTable = CompetitorsTable
  override val table = TableQuery[CompetitorsTable]

  def getByUrl(url: String): Future[Option[Competitor]] =
    db.run(table.filter(_.url === url).result.headOption)
}
